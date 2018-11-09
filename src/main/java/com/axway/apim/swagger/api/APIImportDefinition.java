package com.axway.apim.swagger.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.swagger.APIContract;
import com.axway.apim.swagger.api.properties.APIAuthentication;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	private APIContract apiContract;
	
	/**
	 * The Swagger-File (might a path to a file or a classpath resource) itself
	 */
	private String pathToSwagger;

	public APIImportDefinition(APIContract apiContract, String pathToSwagger) {
		super();
		this.apiContract = apiContract;
		this.pathToSwagger = pathToSwagger;
		this.swaggerDefinition = new APISwaggerDefinion(getSwaggerDefFromFile());
		this.isValid = true;
	}
	
	@Override
	public String getOrgId() {
		try {
			LOG.info("Getting details for organization: " + apiContract.getProperty("/apim/organization/development").asText() + " from API-Manager!");
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/")
					.setParameter("field", "name")
					.setParameter("op", "eq")
					.setParameter("value", apiContract.getProperty("/apim/organization/development").asText()).build();
			GETRequest getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			JsonNode jsonNode = objectMapper.readTree(response);
			if(jsonNode==null) LOG.error("Unable to read details for org: " + apiContract.getProperty("/apim/organization/development").asText());
			return jsonNode.get(0).get("id").asText();
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
	
	private byte[] getSwaggerDefFromFile() {
		try {
			return IOUtils.toByteArray(getSwaggerAsStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getApiPath() {
		JsonNode node = this.apiContract.getProperty("/apim/path");
		this.apiPath = node.asText();
		return this.apiPath;
	}

	@Override
	public String getStatus() {
		JsonNode node = this.apiContract.getProperty("/apim/status");
		String myState = node.asText(); 
		this.status = myState;
		return this.status;
	}
	
	@Override
	public void setStatus(String status) {
		throw new RuntimeException("Set status on ImportAPIDefinition not implemented.");
	}

	@Override
	public APIAuthentication getAuthentication() {
		ArrayNode authN = (ArrayNode)this.apiContract.getProperty("/apim/authentication");
		this.authentication = new APIAuthentication(authN);
		return this.authentication;
	}
	
	
	
	@Override
	public String getApiVersion() {
		JsonNode node = this.apiContract.getProperty("/apim/version");
		return node.asText();
	}

	@Override
	public String getApiName() {
		JsonNode node = this.apiContract.getProperty("/apim/name");
		return node.asText();
	}

	public APIContract getApiContract() {
		return apiContract;
	}

	public void setApiContract(APIContract apiContract) {
		this.apiContract = apiContract;
	}

	public String getPathToSwagger() {
		return pathToSwagger;
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 */
	public InputStream getSwaggerAsStream() {
		File inputFile = new File(pathToSwagger);
		InputStream is = null;
		try {
			if(inputFile.exists()) { 
				is = new FileInputStream(pathToSwagger);
			} else {
				is = this.getClass().getResourceAsStream(pathToSwagger);
			}
			if(is == null) {
				throw new IOException("Unable to read swagger file from: " + pathToSwagger);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	@Override
	public String getApiId() {
		throw new RuntimeException("Import API can't have an ID");
	}
}
