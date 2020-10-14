package com.axway.lib.testActions;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public abstract class CLIAbstractTestAction extends AbstractTestAction implements TestParams {
	
	protected static Logger LOG = LoggerFactory.getLogger(CLIAbstractTestAction.class);
	
	protected File testDirectory;

	public CLIAbstractTestAction() { }

	@Override
	public void doExecute(TestContext context) {
		this.testDirectory = createTestDirectory(context);
		doExecute(context, testDirectory);
	}

	public abstract void doExecute(TestContext context, File testDirectory);
	
	protected int getExpectedReturnCode(TestContext context) {
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable(PARAM_EXPECTED_RC));
		} catch (Exception ignore) {};
		return expectedReturnCode;
	}
	
	protected File createTestDirectory(TestContext context) {
		String testDirName = getTestDirName(context);
		String tmpDir = System.getProperty("java.io.tmpdir");
		File testDir = new File(tmpDir + File.separator + testDirName);
		if(testDir.mkdir()) {
			LOG.info("Successfully created Test-Directory: "+testDir);
		} else {
			throw new ValidationException("Error creating test directory: " + testDir);
		}
		return testDir;
	}
	
	protected abstract String getTestDirName(TestContext context);
	
	protected void addCoreParameter(CoreParameters params, TestContext context) {
		params.setHostname(getHostname(context));
		params.setUsername(getUsername(context));
		params.setPassword(getPassword(context));
		params.setAdminUsername(getAdminUsername(context));
		params.setAdminUsername(getAdminPassword(context));
		params.setIgnoreAdminAccount(getIgnoreAdminAccount(context));
		params.setStage(getStage(context));
		params.setProperties(new EnvironmentProperties(params.getStage(), null));
	}

	public String getStage(TestContext context) {
		try {
			return context.getVariable(PARAM_STAGE);
		} catch (Exception e) {}
		return null;
	}

	public String getHostname(TestContext context) {
		try {
			return context.getVariable(PARAM_HOSTNAME);
		} catch (Exception e) {}
		return null;
	}

	public String getUsername(TestContext context) {
		try {
			return context.getVariable(PARAM_OADMIN_USERNAME);
		} catch (Exception e) {}
		return null;
	}

	public String getPassword(TestContext context) {
		try {
			return context.getVariable(PARAM_OADMIN_PASSWORD);
		} catch (Exception e) {}
		return null;
	}
	
	public String getAdminUsername(TestContext context) {
		try {
			return context.getVariable(PARAM_ADMIN_USERNAME);
		} catch (Exception e) {}
		return null;
	}

	public String getAdminPassword(TestContext context) {
		try {
			return context.getVariable(PARAM_ADMIN_PASSWORD);
		} catch (Exception e) {}
		return null;
	}
	
	public Boolean getIgnoreAdminAccount(TestContext context) {
		try {
			return Boolean.parseBoolean(context.getVariable(PARAM_IGNORE_ADMIN_ACC));
		} catch (Exception e) {}
		return false;
	}
}
