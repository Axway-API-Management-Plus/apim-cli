package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.PUTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.quota.QuotaRestriction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateQuotaConfiguration extends AbstractAPIMTask implements IResponseParser {
	
	private static int QUOTA_UPDATE_SUCCESS = 1;
	private static int QUOTA_UPDATE_FAIL = 2;

	public UpdateQuotaConfiguration(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}

	public void execute() throws AppException {
		Transaction context = Transaction.getInstance();
		if(desiredState.getApplicationQuota()==null || desiredState.getSystemQuota()==null) return;
		if(CommandParameters.getInstance().isIgnoreQuotas()) {
			LOG.info("Configured quotas will be ignored, as flag ignoreQuotas has been set.");
			return;
		}
		
		if(desiredState.getSystemQuota()!=null) {
			if(desiredState.getSystemQuota().equals(actualState.getSystemQuota())) {
				LOG.info("Default-System-Quota for API: '"+desiredState.getName()+"' is UN-CHANGED. Nothing to do.");
			} else {
				LOG.info("Updating System-Default-Quota for API: " + desiredState.getName());
				LOG.debug("System-Quota-Config: '" + desiredState.getSystemQuota()+"'");
				APIQuota systemQuota = APIManagerAdapter.sytemQuotaConfig;
				for(QuotaRestriction restriction : desiredState.getSystemQuota().getRestrictions()) {
					restriction.setApi(actualState.getId());
				}
				addOrMergeRestriction(systemQuota.getRestrictions(), desiredState.getSystemQuota().getRestrictions());
				context.put(QUOTA_UPDATE_SUCCESS, "System-Default quota successfully updated for API: " + desiredState.getName());
				context.put(QUOTA_UPDATE_FAIL, "System-Default quota successfully updated for API: " + desiredState.getName());
				updateQuotaConfig(systemQuota, systemQuota.getId());
			}
		}
		if(desiredState.getApplicationQuota()!=null) {
			if(desiredState.getApplicationQuota().equals(actualState.getApplicationQuota())) {
				LOG.info("Default-Application-Quota for API: '"+desiredState.getName()+"' is UN-CHANGED. Nothing to do.");
			} else {
				LOG.info("Updating Application-Default-Quota for API: " + desiredState.getName());
				LOG.debug("System-Quota-Config: '" + desiredState.getSystemQuota()+"'");
				APIQuota applicationQuota = APIManagerAdapter.applicationQuotaConfig;
				for(QuotaRestriction restriction : desiredState.getApplicationQuota().getRestrictions()) {
					restriction.setApi(actualState.getId());
				}
				addOrMergeRestriction(applicationQuota.getRestrictions(), desiredState.getApplicationQuota().getRestrictions());
				context.put(QUOTA_UPDATE_SUCCESS, "Application-Default quota successfully updated: " + desiredState.getName());
				context.put(QUOTA_UPDATE_FAIL, "Application-Default quota successfully updated: " + desiredState.getName());
				updateQuotaConfig(applicationQuota, applicationQuota.getId());
			}
		}
	}
	
	private void addOrMergeRestriction(List<QuotaRestriction> existingRestrictions, List<QuotaRestriction> newRestrictions) {
		boolean existingUpdated = false;
		for(QuotaRestriction newRestriction : newRestrictions) {
			for(int i=0; i<existingRestrictions.size(); i++) {
				QuotaRestriction existingRestriction = existingRestrictions.get(i);
				if(newRestriction.isSameRestriction(existingRestriction)) {
					existingRestrictions.set(i, newRestriction);
					existingUpdated = true;
				}
			}
			if(!existingUpdated) {
				existingRestrictions.add(newRestriction);
			}
		}
	}
	
	
	private void updateQuotaConfig(APIQuota quotaConfig, String quotaId) throws AppException {
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		
		RestAPICall apiCall;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/quotas/"+quotaId).build();
			
			entity = new StringEntity(objectMapper.writeValueAsString(quotaConfig));
			
			apiCall = new PUTRequest(entity, uri, this);
			apiCall.execute();
		} catch (Exception e) {
			throw new AppException("Can't update Quota-Configuration in API-Manager.", ErrorCode.CANT_UPDATE_QUOTA_CONFIG, e);
		}	
	}
	
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		String response = null;
		Transaction context = Transaction.getInstance();
		if(context.get("responseMessage")!=null) {
			LOG.info(""+context.get("responseMessage"));
			return null;
		} else {
			try {
				response = EntityUtils.toString(httpResponse.getEntity());
				JsonNode jsonNode = objectMapper.readTree(response);
				String backendAPIId = jsonNode.findPath("id").asText();
				Transaction.getInstance().put("backendAPIId", backendAPIId);
				// The action was successful, update the status!
				this.actualState.setState(desiredState.getState());
				LOG.info((String)context.get(QUOTA_UPDATE_SUCCESS));
				return null;
			} catch (Exception e1) {
				throw new AppException((String)context.get(QUOTA_UPDATE_FAIL), ErrorCode.CANT_UPDATE_QUOTA_CONFIG, e1);
			}
		}
	}
}
