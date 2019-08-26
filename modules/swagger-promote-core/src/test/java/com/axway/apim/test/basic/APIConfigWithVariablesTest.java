package com.axway.apim.test.basic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIImportConfigAdapter;
import com.axway.apim.swagger.api.state.DesiredAPI;

public class APIConfigWithVariablesTest {

	private static Logger LOG = LoggerFactory.getLogger(APIConfigWithVariablesTest.class);
	
	@BeforeMethod
	public void cleanSingletons() {
		LOG.info("Deleting singletons before exuecting test.");
		ErrorState.deleteInstance();
	}
	
	@Test
	public void withoutStage() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
			new CommandParameters(cmd, null, props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomething");
		} catch (Exception e) {
			LOG.error("Error running test: withoutStage", e);
			throw e;
		}
	}
	
	@Test
	public void withStage() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties("variabletest");
			CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
			new CommandParameters(cmd, null, props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomethingElse");
		} catch (Exception e) {
			LOG.error("Error running test: withStage", e);
			throw e;
		}
	}
	
	@Test
	public void usingOSEnvVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
			new CommandParameters(cmd, null, props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			String osArch = System.getProperty("os.arch");
			Assert.assertEquals(apiConfig.getOrganization(), "API Development "+osArch);
		} catch (Exception e) {
			LOG.error("Error running test: usingOSEnvVariable", e);
			throw e;
		}
	}
	
	@Test
	public void notDeclaredVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
			new CommandParameters(cmd, null, props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
	
	@Test
	public void configFileWithSpaces() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
			new CommandParameters(cmd, null, props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api config with spaces.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
}
