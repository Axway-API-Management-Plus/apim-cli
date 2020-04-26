package com.axway.apim.actions.tasks;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpgradeAccessToNewerAPI extends AbstractAPIMTask implements IResponseParser {
	
	private IAPI inTransitState; 

	public UpgradeAccessToNewerAPI(IAPI inTransitState, IAPI actualState) {
		super(inTransitState, actualState);
		this.inTransitState = inTransitState;
	}
	public void execute() throws AppException {
		if(desiredState.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.debug("No need to grant access to newly created API, as desired state of API is unpublished.");
			return;
		}
		LOG.info("Upgrade access & subscriptions to newly created API.");
		
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		RestAPICall apiCall;
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/upgrade/"+actualState.getId()).build();
			
			List<NameValuePair> params = new Vector<NameValuePair>();
			params.add(new BasicNameValuePair("upgradeApiId", desiredState.getId()));
			
			entity = new UrlEncodedFormEntity(params, "UTF-8");
			
			apiCall = new POSTRequest(entity, uri, this, true);
			apiCall.setContentType("application/x-www-form-urlencoded");
			
			apiCall.execute();
			// API-Manager has now granted access to all existing orgs and give a subscription to existing app
			// therefore we have to update the new Actual-State to reflect this
			inTransitState.setClientOrganizations(actualState.getClientOrganizations());
			inTransitState.setApplications(actualState.getApplications());
		} catch (Exception e) {
			throw new AppException("Can't upgrade access to newer API!", ErrorCode.CANT_UPGRADE_API_ACCESS, e);
		}
		// Existing applications now got access to the new API, hence we have to update the internal state
		APIManagerAdapter.getInstance().addClientApplications(inTransitState, actualState);
		// Additionally we need to preserve existing (maybe manually created) application quotas
		boolean updateAppQuota = false;
		if(actualState.getApplications().size()!=0) {
			LOG.debug("Found: "+actualState.getApplications().size()+" subscribed applications for this API. Taking over potentially configured quota configuration.");
			for(ClientApplication app : actualState.getApplications()) {
				if(app.getAppQuota()==null) continue;
				// REST-API for App-Quota is also returning Default-Quotas, but we have to ignore them here!
				if(app.getAppQuota().getId().equals(APIManagerAdapter.APPLICATION_DEFAULT_QUOTA) || app.getAppQuota().getId().equals(APIManagerAdapter.SYSTEM_API_QUOTA)) continue;
				for(QuotaRestriction restriction : app.getAppQuota().getRestrictions()) {
					if(restriction.getApi().equals(actualState.getId())) { // This application has a restriction for this specific API
						updateAppQuota = true;
						restriction.setApi(desiredState.getId()); // Take over the quota config to new API
						if(!restriction.getMethod().equals("*")) { // The restriction is for a specific method
							String originalMethodName = APIManagerAdapter.getInstance().getMethodNameForId(actualState.getId(), restriction.getMethod());
							// Try to find the same operation for the newly created API based on the name
							String newMethodId = APIManagerAdapter.getInstance().getMethodIdPerName(desiredState.getId(), originalMethodName);
							restriction.setMethod(newMethodId);
						}
					}
				}
				if(updateAppQuota) {
					LOG.info("Taking over existing quota config for application: '"+app.getName()+"' to newly created API.");
					try {
						uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/quota").build();
						entity = new StringEntity(objectMapper.writeValueAsString(app.getAppQuota()), StandardCharsets.UTF_8);
						
						apiCall = new PUTRequest(entity, uri, this, true);
						apiCall.execute();
						EntityUtils.consume(entity);
					} catch (Exception e) {
						ErrorState.getInstance().setError("Can't update application quota.", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
						throw new AppException("Can't update application quota.", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
					}
				}
			}
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		JsonNode jsonNode = null;
		try {
			if(httpResponse.getStatusLine().getStatusCode()!=204 && httpResponse.getStatusLine().getStatusCode()!=200) {
				throw new AppException("Unexpected response from API-Manager:" + httpResponse.getStatusLine() + httpResponse.getEntity(), ErrorCode.CANT_UPGRADE_API_ACCESS);
			}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return jsonNode;
	}
}
