package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateAPIProxy extends AbstractAPIMTask implements IResponseParser {

	public static RestAPICall execute(IAPIDefinition desired, IAPIDefinition actual) {
		LOG.info("Create API-Proxy (Front-End API)");
		
		URI uri;
		HttpEntity entity;
		
		Transaction context = Transaction.getInstance();
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/").build();
			String json = "{\"apiId\":\"" + context.get("backendAPIId") + "\",\"organizationId\":\"" + desired.getOrgId() + "\"}";
			entity = new StringEntity(json);
			
			RestAPICall createAPIProxy = new POSTRequest(entity, uri);
			createAPIProxy.registerResponseCallback(new CreateAPIProxy());
			return createAPIProxy;
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
	public JsonNode parseResponse(InputStream response) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(response);
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
