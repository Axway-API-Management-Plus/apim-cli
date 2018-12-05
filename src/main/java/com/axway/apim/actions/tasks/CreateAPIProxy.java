package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateAPIProxy extends AbstractAPIMTask implements IResponseParser {

	public CreateAPIProxy(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}
	public void execute() throws AppException {
		LOG.info("Create API-Proxy (Front-End API)");
		
		URI uri;
		HttpEntity entity;
		
		Transaction context = Transaction.getInstance();
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/").build();
			String json = "{\"apiId\":\"" + context.get("backendAPIId") + "\",\"organizationId\":\"" + this.desiredState.getOrgId() + "\"}";
			entity = new StringEntity(json);
			
			RestAPICall createAPIProxy = new POSTRequest(entity, uri, this);
			createAPIProxy.execute();
		} catch (Exception e) {
			throw new AppException("Can't create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		String response = null;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			response = EntityUtils.toString(httpResponse.getEntity());
			jsonNode = objectMapper.readTree(response);
			String virtualAPIId = jsonNode.findPath("id").asText();
			Transaction.getInstance().put("virtualAPIId", virtualAPIId);
			JsonNode auth = jsonNode.findPath("authenticationProfiles").get(0);
			Transaction.getInstance().put("authenticationProfiles", auth);
			Transaction.getInstance().put("lastResponse", jsonNode);
		} catch (IOException e) {
			throw new AppException("Cannot parse JSON-Payload for create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
		return jsonNode;
	}
}
