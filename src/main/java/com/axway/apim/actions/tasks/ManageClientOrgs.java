package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.organization.ApiAccess;
import com.fasterxml.jackson.databind.JsonNode;

public class ManageClientOrgs extends AbstractAPIMTask implements IResponseParser {
	
	private static String MODE					= "MODE";
	private static String MODE_GRANT_ACCESS		= "MODE_GRANT_ACCESS";
	private static String MODE_REMOVE_ACCESS	= "MODE_REMOVE_ACCESS";

	public ManageClientOrgs(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}

	public void execute() throws AppException {
		if(desiredState.getClientOrganizations()==null) return;
		if(desiredState.getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) return;
		if(CommandParameters.getInstance().isIgnoreClientOrgs()) {
			LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
			return;
		}
		if(((APIImportDefinition)desiredState).isRequestForAllOrgs()) {
			LOG.info("Granting permission to all organizations");
			grantClientOrganization(getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations()), actualState.getId(), true);
		} else {
			List<String> missingDesiredOrgs = getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations());
			List<String> removingActualOrgs = getMissingOrgs(actualState.getClientOrganizations(), desiredState.getClientOrganizations());
			if(removingActualOrgs.remove( ((AbstractAPIDefinition)desiredState).getOrganization())); // Don't try to remove the Owning-Organization
			if(missingDesiredOrgs.size()==0) {
				LOG.info("All desired organizations: "+desiredState.getClientOrganizations()+" have already access. Nothing to do.");
			} else {
				grantClientOrganization(missingDesiredOrgs, actualState.getId(), false);
			}
			if(removingActualOrgs.size()>0) {
				if(CommandParameters.getInstance().getClientOrgsMode().equals(CommandParameters.MODE_REPLACE)) {
					LOG.info("Removing access for orgs: "+removingActualOrgs+" from API: " + actualState.getName());
					removeClientOrganization(removingActualOrgs, actualState.getId());
				} else {
					LOG.debug("NOT removing access for existing orgs: "+removingActualOrgs+" from API: " + actualState.getName() + " as clientOrgsMode NOT set to replace.");
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
				formBody += "&grantOrgId="+APIManagerAdapter.getOrgId(orgName);
			}
			Transaction.getInstance().put("orgName", grantAccessToOrgs);
		}
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/grantaccess").build();
			
			entity = new StringEntity(formBody);
			
			apiCall = new POSTRequest(entity, uri, this);
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
			String orgId = APIManagerAdapter.getOrgId(orgName);
			Transaction.getInstance().put("orgName", orgName);
			List<ApiAccess> orgsApis = APIManagerAdapter.getOrgsApiAccess(orgId, false);
			for(ApiAccess apiAccess : orgsApis) {
				if(apiAccess.getApiId().equals(apiId)) {
					try {
						uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/"+orgId+"/apis/"+apiAccess.getId()).build();
						
						
						apiCall = new DELRequest(uri, this);
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
		if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_NO_CONTENT) {
			if(context.get(MODE).equals(MODE_GRANT_ACCESS)) {
				LOG.info("Granted permission to organization: '"+context.get("orgName")+"'");
			} else {			
				LOG.info("Removed permission from organization: '"+context.get("orgName")+"'");
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
		return null;
	}
	
	private static List<String> getMissingOrgs(List<String> orgs, List<String> referenceOrgs) throws AppException {
		List<String> missingOrgs = new ArrayList<String>();
		for(String orgName : orgs) {
			if(referenceOrgs.contains(orgName)) {
				continue;
			}
			if(APIManagerAdapter.getOrgId(orgName)==null) {
				LOG.error("Configured organizations: " + APIManagerAdapter.getAllOrgs());
				throw new AppException("Unknown Org-Name: '" + orgName + "'", ErrorCode.UNKNOWN_ORGANIZATION, false);
			}
			missingOrgs.add(orgName);
		}
		return missingOrgs;
	}
}
