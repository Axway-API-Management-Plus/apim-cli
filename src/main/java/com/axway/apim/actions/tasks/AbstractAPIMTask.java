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
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractAPIMTask {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	protected static CommandParameters cmd = CommandParameters.getInstance();
	
	public static JsonNode initActualAPIContext(IAPIDefinition actual) {
		URI uri;
		ObjectMapper objectMapper = new ObjectMapper();
		Transaction context = Transaction.getInstance();
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+actual.getApiId()).build();
			GETRequest getCall = new GETRequest(uri);
			InputStream response = getCall.execute();
			JsonNode lastJsonReponse = objectMapper.readTree(response);
			context.put("lastResponse", lastJsonReponse);
			context.put("virtualAPIId", lastJsonReponse.get("id").asText());
			return lastJsonReponse;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
}
