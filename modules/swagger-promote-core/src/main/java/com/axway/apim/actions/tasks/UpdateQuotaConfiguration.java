package com.axway.apim.actions.tasks;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.PUTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateQuotaConfiguration extends AbstractAPIMTask implements IResponseParser {
	
	private static int QUOTA_UPDATE_SUCCESS = 1;
	private static int QUOTA_UPDATE_FAIL = 2;

	public UpdateQuotaConfiguration(IAPI desiredState, IAPI actualState) {
		super(desiredState, actualState);
	}

	public void execute() throws AppException {
		Transaction context = Transaction.getInstance();
		if(desiredState.getApplicationQuota()==null && desiredState.getSystemQuota()==null) return;
		if(CommandParameters.getInstance().isIgnoreQuotas() || CommandParameters.getInstance().getQuotaMode().equals(CommandParameters.MODE_IGNORE)) {
			LOG.info("Configured quotas will be ignored, as ignoreQuotas is true or QuotaMode has been set to ignore.");
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
				LOG.debug("Application-Quota-Config: '" + desiredState.getApplicationQuota()+"'");
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
	
	private void addOrMergeRestriction(List<QuotaRestriction> existingRestrictions, List<QuotaRestriction> desiredRestrictions) throws AppException {
		List<QuotaRestriction> newDesiredRestrictions = new ArrayList<QuotaRestriction>();
		boolean existingRestrictionFound = false;
		Iterator<QuotaRestriction> it;
		if(CommandParameters.getInstance().getQuotaMode().equals(CommandParameters.MODE_REPLACE)) {
			LOG.info("Removing existing Quotas for API: '"+this.actualState.getName()+"' as quotaMode is set to replace.");
			it = existingRestrictions.iterator();
			// Remove actual existing restrictions for that API
			while(it.hasNext()) {
				QuotaRestriction existingRestriction = it.next();
				if(existingRestriction.getApi().equals(this.actualState.getId())) {
					it.remove();
				}
			}
			
		}
		it = desiredRestrictions.iterator();
		while(it.hasNext()) {
			QuotaRestriction desiredRestriction = it.next();
			for(QuotaRestriction existingRestriction : existingRestrictions) {
				// Don't care about restrictions for another APIs
				if(!existingRestriction.getApi().equals(this.actualState.getId())) {
					continue;
				}
				if(desiredRestriction.isSameRestriction(existingRestriction)) {
					// If it the same restriction, we need to update this one!
					if(existingRestriction.getType()==QuotaRestrictiontype.throttle) {
						existingRestriction.getConfig().put("messages", desiredRestriction.getConfig().get("messages"));
					} else {
						existingRestriction.getConfig().put("mb", desiredRestriction.getConfig().get("mb"));
					}
					existingRestrictionFound = true;
					break;
				}
			}
			// If we haven't found an existing restriction add a new one!
			if(!existingRestrictionFound) newDesiredRestrictions.add(desiredRestriction);
		}
		// And all new desired restrictions
		existingRestrictions.addAll(newDesiredRestrictions);
	}
	
	
	private void updateQuotaConfig(APIQuota quotaConfig, String quotaId) throws AppException {
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		
		RestAPICall apiCall;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/quotas/"+quotaId).build();
			
			entity = new StringEntity(objectMapper.writeValueAsString(quotaConfig), StandardCharsets.UTF_8);
			
			apiCall = new PUTRequest(entity, uri, this, true);
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
		try {
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
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
}
