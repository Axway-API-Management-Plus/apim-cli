package com.axway.apim.test.serviceprofile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.properties.profiles.ServiceProfile;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackendBasePathAndHostTest {
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);
		
		// Check the Service-Profile
		ServiceProfile defaultProfile = testAPI.getServiceProfiles().get("_default");
		Assert.assertEquals(defaultProfile.getBasePath(), "https://myhost.customer.com:8767/api/v1/myAPI");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI");
	}
	
	@Test
	public void backendHostOnly() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com:8767");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		ServiceProfile defaultProfile = testAPI.getServiceProfiles().get("_default");
		Assert.assertEquals(defaultProfile.getBasePath(), "https://myhost.customer.com:8767");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
	}
	
	@Test
	public void backendHostBasisBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com/");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		ServiceProfile defaultProfile = testAPI.getServiceProfiles().get("_default");
		Assert.assertEquals(defaultProfile.getBasePath(), "https://myhost.customer.com/");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
	}
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
