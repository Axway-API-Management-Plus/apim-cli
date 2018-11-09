package com.axway.apim.actions.tasks;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;

public class UpgradeAccessToNewerAPI extends AbstractAPIMTask implements IResponseParser {

	public UpgradeAccessToNewerAPI(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}
	public void execute() {
		LOG.info("Grant access to new API");
		
		URI uri;
		HttpEntity entity;
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/upgrade/"+actualState.getApiId()).build();
			
			List<NameValuePair> params = new Vector<NameValuePair>();
			params.add(new BasicNameValuePair("upgradeApiId", desiredState.getApiId()));
			
			entity = new UrlEncodedFormEntity(params, "UTF-8");
			
			RestAPICall postRequest = new POSTRequest(entity, uri, this);
			postRequest.setContentType("application/x-www-form-urlencoded");
			
			postRequest.execute();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse response) {
		JsonNode jsonNode = null;
		if(response.getStatusLine().getStatusCode()!=204) {
			throw new RuntimeException("Unexpected response from API-Manager:" + response.getStatusLine() + response.getEntity());
		}
		return jsonNode;
	}
}
