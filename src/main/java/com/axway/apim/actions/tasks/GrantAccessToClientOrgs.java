package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;

public class GrantAccessToClientOrgs extends AbstractAPIMTask implements IResponseParser {

	public GrantAccessToClientOrgs(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}

	public void execute() throws AppException {
		if(desiredState.getClientOrganizations()==null) return;
		if(desiredState.getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) return;
		List<String> grantAccessToOrgs = new ArrayList<String>();
		if(CommandParameters.getInstance().isIgnoreClientOrgs()) {
			LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
			return;
		}
		if(((APIImportDefinition)desiredState).isRequestForAllOrgs()) {
			LOG.info("Granting permission to all organizations");
			grantClientOrganization(grantAccessToOrgs, actualState.getId(), true);
		} else {
			for(String orgName : desiredState.getClientOrganizations()) {
				if(actualState.getClientOrganizations().contains(orgName)) {
					continue;
				}
				if(APIManagerAdapter.getOrgId(orgName)==null) {
					LOG.error("Configured organizations: " + APIManagerAdapter.getAllOrgs());
					throw new AppException("Unknown Org-Name: '" + orgName + "'", ErrorCode.UNKNOWN_ORGANIZATION, false);
				}
				grantAccessToOrgs.add(orgName);
			}
			grantClientOrganization(grantAccessToOrgs, actualState.getId(), false);
		}
	}
	
	
	private void grantClientOrganization(List<String> grantAccessToOrgs, String apiId, boolean allOrgs) throws AppException {
		URI uri;
		HttpEntity entity;
		
		RestAPICall apiCall;
		String formBody;
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
		} catch (Exception e) {
			LOG.error("grantAccessToOrgs: '"+grantAccessToOrgs+"'");
			LOG.error("allOrgs: '"+allOrgs+"'");
			throw new AppException("Can't grant access to organization.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}	
	}
	
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		Transaction context = Transaction.getInstance();
		if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_NO_CONTENT) {
			LOG.info("Granted permission to organization: '"+context.get("orgName")+"'");
		} else {
			LOG.error("Received status code: " + httpResponse.getStatusLine().getStatusCode());
			try {
				LOG.error("Received response: " + EntityUtils.toString(httpResponse.getEntity()));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			throw new AppException("Failure granting permission to organization: '"+context.get("orgName")+"'.", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
		}
		return null;
	}
}
