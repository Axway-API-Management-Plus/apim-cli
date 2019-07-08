package com.axway.apim.test.basic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.swagger.APIImportConfigAdapter;
import com.axway.apim.swagger.api.state.DesiredAPI;

public class APIConfigWithVariablesTest {
	
	@Test
	public void withoutStage() throws AppException, ParseException {
		EnvironmentProperties props = new EnvironmentProperties(null);
		CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
		new CommandParameters(cmd, null, props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
		
		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomething");
	}
	
	@Test
	public void withStage() throws AppException, ParseException {
		EnvironmentProperties props = new EnvironmentProperties("variabletest");
		CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
		new CommandParameters(cmd, null, props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
		
		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomethingElse");
	}
	
	@Test
	public void usingOSEnvVariable() throws AppException, ParseException {
		EnvironmentProperties props = new EnvironmentProperties(null);
		CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
		new CommandParameters(cmd, null, props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
		
		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		String osArch = System.getProperty("os.arch");
		Assert.assertEquals(apiConfig.getOrganization(), "API Development "+osArch);
	}
	
	@Test
	public void notDeclaredVariable() throws AppException, ParseException {
		EnvironmentProperties props = new EnvironmentProperties(null);
		CommandLine cmd = new DefaultParser().parse(new Options(), new String[]{});
		new CommandParameters(cmd, null, props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
		
		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
	}
}
