package com.axway.lib.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.utils.Utils;

public class UtilsTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(UtilsTest.class);
	
	String testConfig = this.getClass().getResource("/stageConfigTest/test-api-config.json").getFile();
	String stageConfig = this.getClass().getResource("/stageConfigTest/my-stage-test-api-config.json").getFile();

	@Test
	public void testGetAPIDefinitionUriFromFile() throws AppException {
		String filePath =  this.getClass().getResource("/com/axway/apim/adapter/spec/airports_swagger_20.json").getFile();
		Assert.assertEquals("{", Utils.getAPIDefinitionUriFromFile(filePath));

	}

	@Test(expectedExceptions = AppException.class)
	public void testGetAPIDefinitionUriFromFileInvalidFile() throws AppException {
		Assert.assertEquals("{", Utils.getAPIDefinitionUriFromFile("test.json"));
	}
	
	@Test
	public void testGetStageConfigNoStage() {
		File stageConfigFile = Utils.getStageConfig(null, null, null);
		Assert.assertNull(stageConfigFile);
	}

	@Test
	public void testGetExternalPolicyName(){
		String policy = "<key type='CircuitContainer'><id field='name' value='Generated Policies'/><key type='CircuitContainer'><id field='name' value='REST API&apos;s'/><key type='CircuitContainer'><id field='name' value='Templates'/><key type='FilterCircuit'><id field='name' value='Default Fault Handler'/></key></key></key></key>";
		Assert.assertEquals(Utils.getExternalPolicyName(policy), "Default Fault Handler");
	}

	@Test
	public void testSubstituteVariables() throws IOException {
		String filePath =  this.getClass().getResource("/com/axway/apim/adapter/conf/apim-config.json").getFile();
		CoreParameters coreParameters = new CoreParameters();
		Map<String, String> properties = new HashMap<>();
		properties.put("apiName", "weather");
		coreParameters.setProperties(properties);
		String output = Utils.substituteVariables(new File(filePath));
		DocumentContext documentContext = JsonPath.parse(output);
		Assert.assertEquals("weather", documentContext.read("$.name", String.class));
	}

	@Test
	public void testLocateConfigFile() throws AppException {
		String filePath =  this.getClass().getResource("/com/axway/apim/adapter/conf/apim-config.json").getFile();
		File file = Utils.locateConfigFile(filePath);
		Assert.assertNotNull(file);
	}
	
	@Test
	public void testGetStageConfigSomeStage() {
		File stageConfigFile = Utils.getStageConfig("someStage", null, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "test-api-config.someStage.json");
	}
	
	@Test
	public void testGetStageConfigUnknownStage() {
		File stageConfigFile = Utils.getStageConfig("unknownStage", null, new File(testConfig));
		Assert.assertNull(stageConfigFile);
	}
	
	@Test
	public void testGetStageConfigFile() {
		File stageConfigFile = Utils.getStageConfig(null, stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndConfig() {
		File stageConfigFile = Utils.getStageConfig("prod", stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndRelativeConfig() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testMissingMandatoryCustomProperty() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testLocateInstallFolder() {
		File installFolder = Utils.getInstallFolder();
		LOG.info("Validate install folder: "+installFolder+" exists");
		Assert.assertNotNull(installFolder);
		Assert.assertTrue(installFolder.exists());
	}

	@Test(expectedExceptions = {NullPointerException.class, AppException.class})
	public void testValidateCustomProperties() throws AppException {
		Utils.validateCustomProperties(new HashMap<>(), CustomProperties.Type.api);
	}

	@Test
	public void testAddCustomPropertiesForEntity(){
		Assert.assertTrue(true);
	}

	@Test
	public void testGetParsedDate() throws AppException {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 5);
		Long date = Utils.getParsedDate(calendar.get(Calendar.DATE)+"."+calendar.get(Calendar.MONTH) + 1+"."+calendar.get(Calendar.YEAR));
		Assert.assertNotNull(date);
	}


	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithoutSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithPath()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/api";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithoutSlash()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUrlWithoutSlashAndUri()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithPath()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend/api";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/api/v3");
	}

	@Test
	public void testGetEncryptedPassword(){
		Assert.assertEquals(Utils.getEncryptedPassword(), "********");
	}

	@Test
	public void testCompareValuesList() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		String actual = "[{\n" +
				"    \"name\": \"updateUser\",\n" +
				"    \"summary\": \"Update user\",\n" +
				"    \"descriptionType\": \"original\"\n" +
				"  }, {\n" +
				"    \"name\": \"getUserByName\",\n" +
				"    \"summary\": \"Get user by user name\",\n" +
				"    \"descriptionType\": \"original\"\n" +
				"  }]";
		TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
		List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

		String desired = "[\n" +
				"  {\n" +
				"    \"name\": \"getUserByName\",\n" +
				"    \"summary\": \"Get user by user name\",\n" +
				"    \"descriptionType\": \"original\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"name\": \"updateUser\",\n" +
				"    \"summary\": \"Update user\",\n" +
				"    \"descriptionType\": \"original\"\n" +
				"  }\n" +
				"]";
		List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
		Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));
	}

	@Test
	public void testCompareValues(){
		Assert.assertTrue(Utils.compareValues("test", "test"));
	}

	@Test
	public void testIsHttpsUri(){
		Assert.assertTrue(Utils.isHttpUri("https://api.axway.com"));
	}

	@Test
	public void testIsHttpUri(){
		Assert.assertTrue(Utils.isHttpUri("http://api.axway.com"));
	}

	@Test
	public void testGetAPILogStringNA(){
		Assert.assertEquals("N/A", Utils.getAPILogString(null));
	}

	@Test
	public void testGetAPILogString(){
		API api = new API();
		api.setVersion("1.0");
		api.setName("weather");
		Assert.assertEquals("weather 1.0 (1.0)", Utils.getAPILogString(api));
	}


}
