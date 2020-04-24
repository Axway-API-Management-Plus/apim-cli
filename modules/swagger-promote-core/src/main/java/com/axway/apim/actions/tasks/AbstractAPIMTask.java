package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractAPIMTask {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	protected IAPI desiredState;
	protected IAPI actualState;
	protected IAPI transitState;
	
	public AbstractAPIMTask(IAPI desiredState, IAPI actualState) {
		super();
		this.desiredState 	= desiredState;
		this.actualState 	= actualState;
	}

	protected static CommandParameters cmd = CommandParameters.getInstance();
	
	public static JsonNode initActualAPIContext(IAPI actual) throws AppException {
		URI uri;
		ObjectMapper objectMapper = new ObjectMapper();
		Transaction context = Transaction.getInstance();
		InputStream response =null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+actual.getId()).build();
			GETRequest getCall = new GETRequest(uri, null);
			response = getCall.execute().getEntity().getContent();
			JsonNode lastJsonReponse = objectMapper.readTree(response);
			context.put("lastResponse", lastJsonReponse);
			context.put("virtualAPIId", lastJsonReponse.get("id").asText());
			return lastJsonReponse;
		} catch (URISyntaxException e) {
			throw new AppException("Can't send HTTP-Request to API-Manager Proxy-Endpoint.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		} catch (IOException e) {
			throw new AppException("IO-Exception, while sending HTTP-Request to API-Manager Proxy-Endpoint", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		} finally {
			try {
				response.close();
			} catch (Exception ignore) { }
		}
	}
}
