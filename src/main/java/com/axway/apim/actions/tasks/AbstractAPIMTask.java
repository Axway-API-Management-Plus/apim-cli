package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractAPIMTask {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	protected IAPIDefinition desiredState;
	protected IAPIDefinition actualState;
	protected IAPIDefinition transitState;
	
	public AbstractAPIMTask(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super();
		this.desiredState 	= desiredState;
		this.actualState 	= actualState;
	}

	protected static CommandParameters cmd = CommandParameters.getInstance();
	
	public static JsonNode initActualAPIContext(IAPIDefinition actual) throws AppException {
		URI uri;
		ObjectMapper objectMapper = new ObjectMapper();
		Transaction context = Transaction.getInstance();
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+actual.getId()).build();
			GETRequest getCall = new GETRequest(uri, null);
			InputStream response = getCall.execute().getEntity().getContent();
			JsonNode lastJsonReponse = objectMapper.readTree(response);
			context.put("lastResponse", lastJsonReponse);
			context.put("virtualAPIId", lastJsonReponse.get("id").asText());
			return lastJsonReponse;
		} catch (URISyntaxException e) {
			throw new AppException("Can't send HTTP-Request to API-Manager Proxy-Endpoint.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		} catch (IOException e) {
			throw new AppException("IO-Exception, while sending HTTP-Request to API-Manager Proxy-Endpoint", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		}
		
	}
}
