package com.axway.apim.test.customproperties;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.fasterxml.jackson.databind.JsonNode;

public class AppConfigParserTest {

	@Test
	public void testAppConfigParser() throws AppException, IOException {
		String fileToTest = "com/axway/apim/test/files/customproperties/app-1.config";
		String appConfigContent = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource(fileToTest), "UTF-8");
		JsonNode jsonNode = APIManagerAdapter.parseAppConfig(appConfigContent);
		JsonNode apiProperties = jsonNode.get("api");
		Assert.assertEquals(apiProperties.at("/customProperty1/label").asText(), "Custom Property #1");
		Assert.assertEquals(apiProperties.at("/customProperty2/type").asText(), "select");
		Assert.assertEquals(apiProperties.at("/customProperty3/type").asText(), "switch");
	}

	@Test
	public void testAppConfigIssue79Parser() throws AppException, IOException {
		String fileToTest = "com/axway/apim/test/files/customproperties/app-2-issue-79.config";
		String appConfigContent = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource(fileToTest), "UTF-8");
		JsonNode jsonNode = APIManagerAdapter.parseAppConfig(appConfigContent);
		JsonNode apiProperties = jsonNode.get("api");
		Assert.assertEquals(apiProperties.at("/contentBasedRouting/label").asText(), "Content Based Routing");
		Assert.assertEquals(apiProperties.at("/contentBasedRouting/required").asText(), "false");
		Assert.assertEquals(apiProperties.at("/contentBasedRouting/help").asText(), "Test");
		Assert.assertEquals(apiProperties.at("/securityClassification/label").asText(), "Security classification");
		Assert.assertEquals(apiProperties.at("/securityClassification/required").asText(), "true");
		Assert.assertEquals(apiProperties.at("/securityClassification/type").asText(), "select");
		Assert.assertEquals(apiProperties.at("/securityClassification/help").asText(), "Controls the agreed security classification of this API");
	}
}
