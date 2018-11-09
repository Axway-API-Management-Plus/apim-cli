package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateAPIProxy extends AbstractAPIMTask implements IResponseParser {

	public CreateAPIProxy(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}
	public void execute() {
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
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(getJSONPayload(response));
			String virtualAPIId = jsonNode.findPath("id").asText();
			Transaction.getInstance().put("virtualAPIId", virtualAPIId);
			JsonNode auth = jsonNode.findPath("authenticationProfiles").get(0);
			Transaction.getInstance().put("authenticationProfiles", auth);
			Transaction.getInstance().put("lastResponse", jsonNode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonNode;
	}
}
