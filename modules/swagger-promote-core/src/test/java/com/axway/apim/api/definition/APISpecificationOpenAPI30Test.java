package com.axway.apim.api.definition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class APISpecificationOpenAPI30Test {
	
	private static final String testPackage = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {

		byte[] content = getSwaggerContent(testPackage + "/openapi30.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://myhost.customer.com:8767/api/v1/myAPI");
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS30Specification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).size(), 1, "Expected to get only one server url");
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).get(0).get("url").asText(), "https://myhost.customer.com:8767/api/v1/myAPI");
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
