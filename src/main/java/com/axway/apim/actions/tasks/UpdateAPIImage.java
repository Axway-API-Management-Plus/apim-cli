package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateAPIImage extends AbstractAPIMTask implements IResponseParser {

	public UpdateAPIImage(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
	}
	public void execute() throws AppException {
		LOG.info("Updating API-Image");
		
		URI uri;
		HttpEntity entity;
		
		Transaction context = Transaction.getInstance();
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getApiId()+"/image").build();
			
			entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", ((APIImportDefinition)this.desiredState).getApiImage().getInputStream(), ContentType.create("image/jpeg"), desiredState.getApiImage().getFilename())
					.build();
			
			RestAPICall apiCall = new POSTRequest(entity, uri, this);
			apiCall.setContentType(null);
			apiCall.execute();
		} catch (Exception e) {
			throw new AppException("Can't update API-Image.", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(getJSONPayload(response));
			String status = jsonNode.findPath("status").asText();
		} catch (IOException e) {
			throw new AppException("Cannot parse JSON-Payload for create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
		return jsonNode;
	}
}
