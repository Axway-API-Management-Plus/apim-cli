package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.PUTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.quota.QuotaRestriction;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpgradeAccessToNewerAPI extends AbstractAPIMTask implements IResponseParser {

	public UpgradeAccessToNewerAPI(IAPI inTransitState, IAPI actualState) {
		super(inTransitState, actualState);
	}
	public void execute() throws AppException {
		if(desiredState.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.debug("No need to grant access to newly created API, as desired state of API is unpublished.");
			return;
		}
		LOG.info("Granting access to newly created API");
		
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
		} catch (Exception e) {
			throw new AppException("Can't upgrade access to newer API!", ErrorCode.CANT_UPGRADE_API_ACCESS, e);
		}
		// Additionally we need to preserve existing (maybe manually created) application quotas
		boolean updateAppQuota = false;
		if(actualState.getApplications().size()!=0) {
			LOG.debug("Found: "+actualState.getApplications().size()+" subscribed applications for this API. Taking over potentially configured quota configuration.");
			for(ClientApplication app : actualState.getApplications()) {
				if(app.getAppQuota()==null) continue;
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
						entity = new StringEntity(objectMapper.writeValueAsString(app.getAppQuota()));
						
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
	public JsonNode parseResponse(HttpResponse response) throws AppException {
		JsonNode jsonNode = null;
		if(response.getStatusLine().getStatusCode()!=204 && response.getStatusLine().getStatusCode()!=200) {
			throw new AppException("Unexpected response from API-Manager:" + response.getStatusLine() + response.getEntity(), ErrorCode.CANT_UPGRADE_API_ACCESS);
		}
		return jsonNode;
	}
}
