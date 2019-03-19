package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
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
		if(desiredState.getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) return;
		if(CommandParameters.getInstance().isIgnoreClientApps()) {
			LOG.info("Configured client applications are ignored, as flag ignoreClientApps has been set.");
			return;
		}
		ListIterator<ClientApplication> it = desiredState.getApplications().listIterator();
		ClientApplication app;
		while(it.hasNext()) {
			app = it.next();
			if(!hasClientAppPermission(app)) {
				LOG.error("Organization of configured application: '" + app.getName() + "' has NO permission to this API. Ignoring this application.");
				it.remove();
				continue;
			}
		}
		List<ClientApplication> missingDesiredApps = getMissingApps(desiredState.getApplications(), actualState.getApplications());
		List<ClientApplication> revomingActualApps = getMissingApps(actualState.getApplications(), desiredState.getApplications());
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
	
	private boolean hasClientAppPermission(ClientApplication app) throws AppException {
		String appsOrgId = app.getOrganizationId();
		String appsOrgName = APIManagerAdapter.getOrgName(appsOrgId);
		if(appsOrgName==null) return false;
		return actualState.getClientOrganizations().contains(appsOrgName);
	}
	
	private void createAppSubscription(List<ClientApplication> missingDesiredApps, String apiId) throws AppException {
		URI uri;
		HttpEntity entity;
		
		RestAPICall apiCall;
		Transaction.getInstance().put(MODE, MODE_CREATE_API_ACCESS);
		LOG.info("Creating API-Access for the following apps: '"+missingDesiredApps.toString()+"'");
		try {
			for(ClientApplication app : missingDesiredApps) {
				LOG.debug("Creating API-Access for application '"+app.getName()+"'");
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
	
	private void removeAppSubscrioption(List<ClientApplication> revomingActualApps, String apiId) throws AppException {
		URI uri;
		RestAPICall apiCall;
		Transaction.getInstance().put(MODE, MODE_REMOVE_API_ACCESS);
		for(ClientApplication app : revomingActualApps) {
			LOG.debug("Removing API-Access for application '"+app.getName()+"'");
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
					LOG.debug("Successfully created API-Access for application: '"+context.get("appName")+"'");
				} else {
					LOG.debug("Successfully removed API-Access from application: '"+context.get("appName")+"'");
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
	
	private static List<ClientApplication> getMissingApps(List<ClientApplication> apps, List<ClientApplication> otherApps) throws AppException {
		List<ClientApplication> missingApps = new ArrayList<ClientApplication>();
		for(ClientApplication app : apps) {
			if(otherApps.contains(app)) {
				continue;
			}
			missingApps.add(app);
		}
		return missingApps;
	}
}
