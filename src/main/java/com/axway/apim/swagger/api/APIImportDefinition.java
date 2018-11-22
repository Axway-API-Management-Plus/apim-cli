package com.axway.apim.swagger.api;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIImportConfig;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author cwiechmann
 * This class reflects the given API-Swagger-Specification plus 
 * the API-Contract and it used by the APIManagerImporter to 
 * import it.
 * 
 * TODO: Support JSON & YAML files 
 */
public class APIImportDefinition extends AbstractAPIDefinition implements IAPIDefinition {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportDefinition.class);

	/**
	 * Required meta information
	 */
	private APIImportConfig apiContract;
	
	public APIImportDefinition() throws AppException {
		super();
	}

	@Override
	public String getOrgId() throws AppException {
		try {
			LOG.info("Getting details for organization: " + this.organization + " from API-Manager!");
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/")
					.setParameter("field", "name")
					.setParameter("op", "eq")
					.setParameter("value", this.organization).build();
			GETRequest getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			JsonNode jsonNode = objectMapper.readTree(response);
			if(jsonNode==null) LOG.error("Unable to read details for org: " + apiContract.getProperty("/organization/development").asText());
			return jsonNode.get(0).get("id").asText();
		} catch (Exception e) {
			throw new AppException("Can't read Org-Details from API-Manager. Is the API-Managre running?", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
}
