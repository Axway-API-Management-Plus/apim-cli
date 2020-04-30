package com.axway.apim.apiimport.actions.tasks;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.IResponseParser;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateAPIImage extends AbstractAPIMTask implements IResponseParser {

	public UpdateAPIImage(IAPI desiredState, IAPI actualState) {
		super(desiredState, actualState);
	}
	public void execute() throws AppException {
		if(!desiredState.getImage().isValid()) {
			LOG.info("No image configured, doing nothing.");
			return;
		}
		LOG.info("Updating API-Image from: " + desiredState.getImage().getFilename());
		
		URI uri;
		HttpEntity entity;
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId()+"/image").build();
			
			entity = MultipartEntityBuilder.create()
						.addBinaryBody("file", ((DesiredAPI)this.desiredState).getImage().getInputStream(), ContentType.create("image/jpeg"), desiredState.getImage().getBaseFilename())
					.build();
			
			RestAPICall apiCall = new POSTRequest(entity, uri, this);
			apiCall.setContentType(null);
			apiCall.execute();
		} catch (Exception e) {
			throw new AppException("Can't update API-Image.", ErrorCode.UNXPECTED_ERROR, e);
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
		} catch (IOException e) {
			throw new AppException("Cannot parse JSON-Payload for create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return jsonNode;
	}
}
