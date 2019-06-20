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
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public class ManageClientApps extends AbstractAPIMTask implements IResponseParser {
	
	private static String MODE						= "MODE";
	private static String MODE_CREATE_API_ACCESS	= "MODE_CREATE_API_ACCESS";
	private static String MODE_REMOVE_API_ACCESS	= "MODE_REMOVE_API_ACCESS";
	
	/**
	 * In case, the API has been re-created, this is object contains the API how it was before
	 */
	IAPI oldAPI;
	
	public ManageClientApps(IAPI desiredState, IAPI actualState, IAPI oldAPI) {
		super(desiredState, actualState);
		this.oldAPI = oldAPI;
	}
	
	public void execute(boolean reCreation) throws AppException {
		if(desiredState.getApplications()==null && !reCreation) return;
		if(CommandParameters.getInstance().isIgnoreClientApps()) {
			LOG.info("Configured client applications are ignored, as flag ignoreClientApps has been set.");
			return;
		}
		if(desiredState.getApplications()!=null) { // Happens, when config-file doesn't contains client apps
			removeNonGrantedClientApps(desiredState.getApplications());
		}
		List<ClientApplication> recreateActualApps = null;
		// If an UNPUBLISHED API has been re-creared, we have to create App-Subscriptions manually, as API-Manager Upgrade only works on PUBLISHED APIs
		// But we only need to do this, if existing App-Subscriptions should be preserved (MODE_ADD).
		if(reCreation && actualState.getState().equals(IAPI.STATE_UNPUBLISHED) && CommandParameters.getInstance().getClientAppsMode().equals(CommandParameters.MODE_ADD)) {
			removeNonGrantedClientApps(oldAPI.getApplications());
			recreateActualApps = getMissingApps(oldAPI.getApplications(), actualState.getApplications());
			// Create previously existing App-Subscriptions
			createAppSubscription(recreateActualApps, actualState.getId());
			// Update the actual state 
			actualState.setApplications(recreateActualApps);
		}
		List<ClientApplication> missingDesiredApps = getMissingApps(desiredState.getApplications(), actualState.getApplications());
		List<ClientApplication> revomingActualApps = getMissingApps(actualState.getApplications(), desiredState.getApplications());

		if(missingDesiredApps.size()==0 && desiredState.getApplications()!=null) {
			LOG.info("All desired applications: "+desiredState.getApplications()+" have already a subscription. Nothing to do.");
		} else {
			createAppSubscription(missingDesiredApps, actualState.getId());
		}
		if(revomingActualApps.size()>0) {
			if(CommandParameters.getInstance().getClientAppsMode().equals(CommandParameters.MODE_REPLACE)) {
				LOG.info("Removing access for appplications: "+revomingActualApps+" from API: " + actualState.getName());
				removeAppSubscrioption(revomingActualApps, actualState.getId());
			} else {
				LOG.debug("NOT removing access for appplications: "+revomingActualApps+" from API: " + actualState.getName() + " as clientAppsMode NOT set to replace.");
			}
		}
	}
	
	private void removeNonGrantedClientApps(List<ClientApplication> apps) throws AppException {
		if(apps == null) return;
		ListIterator<ClientApplication> it = apps.listIterator();
		ClientApplication app;
		while(it.hasNext()) {
			app = it.next();
			if(!hasClientAppPermission(app)) {
				LOG.error("Organization of configured application: '" + app.getName() + "' has NO permission to this API. Ignoring this application.");
				it.remove();
				continue;
			}
		}
	}
	
	private boolean hasClientAppPermission(ClientApplication app) throws AppException {
		String appsOrgId = app.getOrganizationId();
		String appsOrgName = APIManagerAdapter.getInstance().getOrgName(appsOrgId);
		if(appsOrgName==null) return false;
		// If the App belongs to the same Org as the API, it automatically has permission (esp. for Unpublished APIs)
		if(app.getOrganizationId().equals(((ActualAPI)actualState).getOrganizationId())) return true;
		if(actualState.getClientOrganizations()==null) {
			LOG.debug("No org has access to this API, hence no other app has permission.");
			return true;
		}
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
				Transaction.getInstance().put("appName", app);
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/apis").build();
				entity = new StringEntity("{\"apiId\":\""+apiId+"\",\"enabled\":true}");
				
				apiCall = new POSTRequest(entity, uri, this, true);
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
				Transaction.getInstance().put("appName", app);
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/apis/"+apiId).build();
				apiCall = new DELRequest(uri, this, true);
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
					actualState.getApplications().add((ClientApplication)context.get("appName"));
					LOG.debug("Successfully created API-Access for application: '"+context.get("appName")+"'");
				} else {
					actualState.getApplications().remove((ClientApplication)context.get("appName"));
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
		if(apps == null || otherApps == null) return missingApps;
		for(ClientApplication app : apps) {
			if(otherApps.contains(app)) {
				continue;
			}
			missingApps.add(app);
		}
		return missingApps;
	}
}
