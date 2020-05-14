package com.axway.apim.apiimport.actions.tasks;

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

import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.IAPI;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.IResponseParser;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.databind.JsonNode;

public class ManageClientOrgs extends AbstractAPIMTask implements IResponseParser {
	
	private static String MODE					= "MODE";
	private static String MODE_GRANT_ACCESS		= "MODE_GRANT_ACCESS";
	private static String MODE_REMOVE_ACCESS	= "MODE_REMOVE_ACCESS";
	
	APIManagerOrganizationAdapter orgsAdapter = new APIManagerOrganizationAdapter();
	APIManagerAPIAccessAdapter accessAdapter = new APIManagerAPIAccessAdapter();

	public ManageClientOrgs(API desiredState, API actualState) {
		super(desiredState, actualState);
	}

	public void execute(boolean reCreation) throws AppException {
		if(CommandParameters.getInstance().isIgnoreClientOrgs()) {
			LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
			return;
		}
		if(desiredState.getState().equals(IAPI.STATE_UNPUBLISHED)) return;
		// The API isn't Re-Created (to take over manually created ClientOrgs) and there are no orgs configured - We can skip the rest
		if(desiredState.getClientOrganizations()==null && !reCreation) return;
		// From here, the assumption is that existing Org-Access has been upgraded already - We only have to take care about additional orgs
		if(((DesiredAPI)desiredState).isRequestForAllOrgs()) {
			LOG.info("Granting permission to all organizations");
			grantClientOrganization(getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations()), actualState.getId(), true);
		} else {
			List<String> missingDesiredOrgs = getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations());
			List<String> removingActualOrgs = getMissingOrgs(actualState.getClientOrganizations(), desiredState.getClientOrganizations());
			if(removingActualOrgs.remove( ((API)desiredState).getOrganization())); // Don't try to remove the Owning-Organization
			if(missingDesiredOrgs.size()==0) {
				if(desiredState.getClientOrganizations()!=null) {
					LOG.info("All desired organizations: "+desiredState.getClientOrganizations()+" have already access. Nothing to do.");
				}
			} else {
				grantClientOrganization(missingDesiredOrgs, actualState.getId(), false);
			}
			if(removingActualOrgs.size()>0) {
				if(CommandParameters.getInstance().getClientOrgsMode().equals(CommandParameters.MODE_REPLACE)) {
					LOG.info("Removing access for orgs: "+removingActualOrgs+" from API: " + actualState.getName());
					removeClientOrganization(removingActualOrgs, actualState.getId());
				} else {
					LOG.info("NOT removing access for existing orgs: "+removingActualOrgs+" from API: " + actualState.getName() + " as clientOrgsMode NOT set to replace.");
				}
			}
		}
	}
	
	
	private void grantClientOrganization(List<String> grantAccessToOrgs, String apiId, boolean allOrgs) throws AppException {
		URI uri;
		HttpEntity entity;
		
		RestAPICall apiCall;
		String formBody;
		Transaction.getInstance().put(MODE, MODE_GRANT_ACCESS);
		if(allOrgs) {
			formBody = "action=all_orgs&apiId="+apiId;
			Transaction.getInstance().put("orgName", "ALL");
		} else {
			formBody = "action=orgs&apiId="+apiId;
			for(String orgName : grantAccessToOrgs) {
				formBody += "&grantOrgId="+orgsAdapter.getOrgId(orgName);
			}
			Transaction.getInstance().put("orgName", grantAccessToOrgs);
		}
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/grantaccess").build();
			
			entity = new StringEntity(formBody);
			
			apiCall = new POSTRequest(entity, uri, this, true);
			apiCall.setContentType("application/x-www-form-urlencoded");
			apiCall.execute();
			// Update the actual state to reflect, which organizations now really have access to the API (this also includes prev. added orgs)
			actualState.getClientOrganizations().addAll(grantAccessToOrgs);
		} catch (Exception e) {
			LOG.error("grantAccessToOrgs: '"+grantAccessToOrgs+"'");
			LOG.error("allOrgs: '"+allOrgs+"'");
			throw new AppException("Can't grant access to organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}	
	}
	
	private void removeClientOrganization(List<String> removingActualOrgs, String apiId) throws AppException {
		URI uri;
		Transaction.getInstance().put(MODE, MODE_REMOVE_ACCESS);
		RestAPICall apiCall;
		for(String orgName : removingActualOrgs) {
			String orgId = orgsAdapter.getOrgId(orgName);
			Transaction.getInstance().put("orgName", orgName);
			List<APIAccess> orgsApis = accessAdapter.getAPIAccess(orgId, Type.organizations);
			for(APIAccess apiAccess : orgsApis) {
				if(apiAccess.getApiId().equals(apiId)) {
					try {
						uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/"+orgId+"/apis/"+apiAccess.getId()).build();
						
						
						apiCall = new DELRequest(uri, this, true);
						apiCall.execute();
						// Update the actual state to reflect, which organizations now really have access to the API (this also includes prev. added orgs)
						actualState.getClientOrganizations().removeAll(removingActualOrgs);
					} catch (Exception e) {
						LOG.error("Can't delete API-Access for organization. ");
						throw new AppException("Can't delete API-Access for organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
					}	
				}
			}
		}
	}
	
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		Transaction context = Transaction.getInstance();
		try {
			if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_NO_CONTENT) {
				if(context.get(MODE).equals(MODE_GRANT_ACCESS)) {
					LOG.debug("Granted permission to organization: '"+context.get("orgName")+"'");
				} else {			
					LOG.debug("Removed permission from organization: '"+context.get("orgName")+"'");
				}
			} else {
				LOG.error("Received status code: " + httpResponse.getStatusLine().getStatusCode());
				try {
					LOG.error("Received response: " + EntityUtils.toString(httpResponse.getEntity()));
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				throw new AppException("Failure granting/deleting permission to/from organization: '"+context.get("orgName")+"'. Mode: '"+context.get(MODE)+"'", 
						ErrorCode.ACCESS_ORGANIZATION_ERR);
			}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return null;
	}
	
	private List<String> getMissingOrgs(List<String> orgs, List<String> referenceOrgs) throws AppException {
		List<String> missingOrgs = new ArrayList<String>();
		if(orgs==null || referenceOrgs ==null) return missingOrgs;
		for(String orgName : orgs) {
			if(referenceOrgs.contains(orgName)) {
				continue;
			}
			if(orgsAdapter.getOrgId(orgName)==null) {
				LOG.warn("Configured organizations: " + orgsAdapter.getAllOrgs());
				ErrorState.getInstance().setError("Unknown Org-Name: '" + orgName + "'", ErrorCode.UNKNOWN_ORGANIZATION, false);
				throw new AppException("Unknown Org-Name: '" + orgName + "'", ErrorCode.UNKNOWN_ORGANIZATION);
			}
			missingOrgs.add(orgName);
		}
		return missingOrgs;
	}
}
