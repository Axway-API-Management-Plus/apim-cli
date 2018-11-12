package com.axway.apim.swagger.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIContract;
import com.axway.apim.swagger.api.properties.APIAuthentication;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

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

	public APIImportDefinition(APIContract apiContract, String pathToSwagger) throws AppException {
		super();
		this.apiContract = apiContract;
		this.pathToSwagger = pathToSwagger;
		this.swaggerDefinition = new APISwaggerDefinion(getSwaggerDefFromFile());
		this.apiImage = new APIImage(getImageFromFile(), this.apiContract.getProperty("/apim/image").asText());
		this.isValid = true;
	}
	
	@Override
	public String getOrgId() throws AppException {
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
		} catch (Exception e) {
			throw new AppException("Can't read Org-Details from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private byte[] getSwaggerDefFromFile() throws AppException {
		try {
			return IOUtils.toByteArray(getSwaggerAsStream());
		} catch (IOException e) {
			throw new AppException("Can't read swagger-file from file", ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
	}
	
	private byte[] getImageFromFile() throws AppException {
		JsonNode imageNode = this.apiContract.getProperty("/apim/image");
		if(imageNode instanceof MissingNode) {
			return null; // No image declared! Means we have to remove the image if one is present in API-Manager
		} else {
			try {
				String baseDir = new File(this.pathToSwagger).getParent();
				File file = new File(baseDir + "/" + imageNode.asText());
				if(file.exists()) { 
					return IOUtils.toByteArray(new FileInputStream(file));
				} else {
					// Try to read it from classpath
					return IOUtils.toByteArray(this.getClass().getResourceAsStream(imageNode.asText()));
			}
			} catch (IOException e) {
				throw new AppException("Can't read image-file from file", ErrorCode.UNXPECTED_ERROR, e);
			}
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
	public void setStatus(String status) throws AppException {
		throw new AppException("Set status on ImportAPIDefinition not implemented.", ErrorCode.UNSUPPORTED_FEATURE);
	}

	@Override
	public APIAuthentication getAuthentication() throws AppException {
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
	
	@Override
	public String getApiSummary() {
		JsonNode node = this.apiContract.getProperty("/apim/summary");
		if(node instanceof MissingNode) return null;
		return node.asText();
	}

	@Override
	public APIImage getApiImage() {
		return apiImage;
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
	 * @throws AppException 
	 */
	public InputStream getSwaggerAsStream() throws AppException {
		File inputFile = new File(pathToSwagger);
		InputStream is = null;
		try {
			if(inputFile.exists()) { 
				is = new FileInputStream(pathToSwagger);
			} else {
				is = this.getClass().getResourceAsStream(pathToSwagger);
			}
			if(is == null) {
				throw new AppException("Unable to read swagger file from: " + pathToSwagger, ErrorCode.CANT_READ_SWAGGER_FILE);
			}
			
		} catch (Exception e) {
			throw new AppException("Unable to read swagger file from: " + pathToSwagger, ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
		return is;
	}

	@Override
	public String getApiId() throws AppException {
		throw new AppException("Import API can't have an ID", ErrorCode.UNSUPPORTED_FEATURE);
	}
}
