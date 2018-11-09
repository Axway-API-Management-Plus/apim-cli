package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class UpdateAPIStatus extends AbstractAPIMTask implements IResponseParser {
	
	public static HashMap<String, String[]> statusChangeMap = new HashMap<String, String[]>() {{
		put("unpublished", 	new String[] {"published", "deleted"});
		put("published", 	new String[] {"unpublished", "deprecatde"});
		put("deleted", 		new String[] {});
		put("deprecated", 	new String[] {"unpublished", "undeprecated"});
	}};
	
	/**
	 * Maps the provided status to the REST-API endpoint to change the status!
	 */
	public static HashMap<String, String> statusEndpoint = new HashMap<String, String>() {{
		put("unpublished", 	"unpublish");
		put("published", 	"publish");
		put("deprecated", 	"deprecate");
		put("undeprecated", "undeprecate");
	}};

	public static RestAPICall execute(IAPIDefinition desired, IAPIDefinition actual) {
		if(actual.getStatus().equals(desired.getStatus())) {
			LOG.debug("Desired and actual status equals. No need to update status!");
			return null;
		}
		LOG.info("Updating API-Status from: '" + actual.getStatus() + "' to '" + desired.getStatus() + "'");
		
		URI uri;
		//ObjectMapper objectMapper = new ObjectMapper();

		Transaction context = Transaction.getInstance();
		
		try {
			JsonNode lastJsonReponse = (JsonNode)context.get("lastResponse");
			if(lastJsonReponse==null) { // This class is called as the first, so, first load the API
				lastJsonReponse = initActualAPIContext(actual);
			}

			String[] possibleStatus = statusChangeMap.get(actual.getStatus());
			boolean statusChangePossible = false;
			for(String status : possibleStatus) {
				if(desired.getStatus().equals(status)) {
					statusChangePossible = true;
				}
			}
			if (!statusChangePossible) {
				LOG.error("The status change from: " + actual.getStatus() + " to " + desired.getStatus() + " is not possible!");
				throw new RuntimeException("The status change from: " + actual.getStatus() + " to " + desired.getStatus() + " is not possible!");
			}
			
			uri = new URIBuilder(cmd.getAPIManagerURL())
					.setPath(RestAPICall.API_VERSION+"/proxies/"+context
					.get("virtualAPIId")+"/"+statusEndpoint.get(desired.getStatus()))
					.build();
			//entity = new StringEntity(objectMapper.writeValueAsString(lastJsonReponse));
			
			RestAPICall changeStatus = new POSTRequest(null, uri);
			changeStatus.setContentType("application/x-www-form-urlencoded");
			changeStatus.registerResponseCallback(new UpdateAPIStatus());
			return changeStatus;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		/*} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);*/
		}
	}
	
	public JsonNode parseResponse(InputStream response) {
		String backendAPIId = JsonPath.parse(response).read("$.id", String.class);
		Transaction.getInstance().put("backendAPIId", backendAPIId);
		return null;
	}
}
