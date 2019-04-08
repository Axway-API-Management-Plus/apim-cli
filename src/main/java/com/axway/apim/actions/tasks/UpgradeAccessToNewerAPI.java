package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public class UpgradeAccessToNewerAPI extends AbstractAPIMTask implements IResponseParser {

	public UpgradeAccessToNewerAPI(IAPI desiredState, IAPI actualState) {
		super(desiredState, actualState);
	}
	public void execute() throws AppException {
		if(desiredState.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.debug("No need to grant access to newly created API, as desired state of API is unpublished.");
			return;
		}
		LOG.info("Granting access to newly created API");
		
		URI uri;
		HttpEntity entity;
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/upgrade/"+actualState.getId()).build();
			
			List<NameValuePair> params = new Vector<NameValuePair>();
			params.add(new BasicNameValuePair("upgradeApiId", desiredState.getId()));
			
			entity = new UrlEncodedFormEntity(params, "UTF-8");
			
			RestAPICall postRequest = new POSTRequest(entity, uri, this, true);
			postRequest.setContentType("application/x-www-form-urlencoded");
			
			postRequest.execute();
		} catch (Exception e) {
			throw new AppException("Can't upgrade access to newer API!", ErrorCode.CANT_UPGRADE_API_ACCESS, e);
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse response) throws AppException {
		JsonNode jsonNode = null;
		if(response.getStatusLine().getStatusCode()!=204) {
			throw new AppException("Unexpected response from API-Manager:" + response.getStatusLine() + response.getEntity(), ErrorCode.CANT_UPGRADE_API_ACCESS);
		}
		return jsonNode;
	}
}
