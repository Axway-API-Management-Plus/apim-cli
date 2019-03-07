package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.DELRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.applications.ClientApplications;
import com.fasterxml.jackson.databind.JsonNode;

public class ManageClientApps extends AbstractAPIMTask implements IResponseParser {
	
	private static String MODE						= "MODE";
	private static String MODE_CREATE_API_ACCESS	= "MODE_CREATE_API_ACCESS";
	private static String MODE_REMOVE_API_ACCESS	= "MODE_REMOVE_API_ACCESS";
	
	public ManageClientApps(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}
	
	public void execute() throws AppException {
		if(desiredState.getApplications()==null) return;
		List<ClientApplications> missingDesiredApps = getMissingApps(desiredState.getApplications(), actualState.getApplications());
		List<ClientApplications> revomingActualApps = getMissingApps(actualState.getApplications(), desiredState.getApplications());
		if(missingDesiredApps.size()==0) {
			LOG.info("All desired applications: "+desiredState.getApplications()+" have already a subscription. Nothing to do.");
		} else {
			createAppSubscription(missingDesiredApps, actualState.getId());
		}
		if(revomingActualApps.size()>0) {
			LOG.info("Removing access for appplications: "+revomingActualApps+" from API: " + actualState.getName());
			removeAppSubscrioption(revomingActualApps, actualState.getId());
		}
	}
	
	private void createAppSubscription(List<ClientApplications> missingDesiredApps, String apiId) throws AppException {
		URI uri;
		HttpEntity entity;
		
		RestAPICall apiCall;
		Transaction.getInstance().put(MODE, MODE_CREATE_API_ACCESS);
		try {
			for(ClientApplications app : missingDesiredApps) {
				Transaction.getInstance().put("appName", app.getName());
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/apis").build();
				entity = new StringEntity("{\"apiId\":\""+apiId+"\",\"enabled\":true}");
				
				apiCall = new POSTRequest(entity, uri, this);
				apiCall.execute();
			}
		} catch (Exception e) {
			throw new AppException("Can't create API access requests.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}	
	}
	
	private void removeAppSubscrioption(List<ClientApplications> revomingActualApps, String apiId) throws AppException {
		URI uri;
		RestAPICall apiCall;
		Transaction.getInstance().put(MODE, MODE_REMOVE_API_ACCESS);
		for(ClientApplications app : revomingActualApps) {
			try { 
				Transaction.getInstance().put("appName", app.getName());
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/apis/"+apiId).build();
				apiCall = new DELRequest(uri, this);
				apiCall.execute();
			} catch (Exception e) {
				LOG.error("Can't delete API access requests for application.");
				throw new AppException("Can't delete API access requests for application.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}	
		}
	}

	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		Transaction context = Transaction.getInstance();
		try {
			if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_CREATED) {
				if(context.get(MODE).equals(MODE_CREATE_API_ACCESS)) {
					LOG.info("Created API-Access for application: '"+context.get("appName")+"'");
				} else {
					LOG.info("Removed API-Access from application: '"+context.get("appName")+"'");
				}
			} else {
				LOG.error("Received status code: " + httpResponse.getStatusLine().getStatusCode());
				try {
					LOG.error("Received response: " + EntityUtils.toString(httpResponse.getEntity()));
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				throw new AppException("Failure creating/deleting API-Access to/from application: '"+context.get("appName")+"'. Mode: '"+context.get(MODE)+"'", 
						ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return null;
	}
	
	private static List<ClientApplications> getMissingApps(List<ClientApplications> apps, List<ClientApplications> otherApps) throws AppException {
		List<ClientApplications> missingApps = new ArrayList<ClientApplications>();
		for(ClientApplications app : apps) {
			if(otherApps.contains(app)) {
				continue;
			}
			if(app.getName()!=null) {
				ClientApplications existingApp = APIManagerAdapter.getApplication(app.getName());
				if(existingApp==null) {
					LOG.warn("Unknown application with name: '" + app.getName() + "' configured. Ignoring this application.");
					continue;
				} else {
					missingApps.add(existingApp);
					continue;
				}
			}
			if(app.getOauthClientId()!=null) {
				addMissingApplication(app.getOauthClientId(), APIManagerAdapter.CREDENTIAL_TYPE_OAUTH, missingApps);
			}
			if(app.getExtClientId()!=null) {
				addMissingApplication(app.getExtClientId(), APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID, missingApps);
			}
			if(app.getApiKey()!=null) {
				addMissingApplication(app.getApiKey(), APIManagerAdapter.CREDENTIAL_TYPE_API_KEY, missingApps);
			}
		}
		return missingApps;
	}
	
	private static void addMissingApplication(String credential, String type, List<ClientApplications> missingApps) throws AppException {
		String appId = APIManagerAdapter.getAppIdForCredential(credential, type);
		if(appId==null) {
			LOG.warn("Unknown application with ("+type+"): '" + credential + "' configured. Ignoring this application.");
			return;
		}
		ClientApplications app = APIManagerAdapter.getAppForId(appId);
		missingApps.add(app);
		return;
	}
}
