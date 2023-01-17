package com.axway.lib.testActions;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

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

	public TestContext context;
	
	public int randomNum;
	
	public CLIAbstractTestAction(TestContext context) {
		super();
		this.context = context;
		this.randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
		this.testDirectory = createTestDirectory(this.context);
	}

	@Override
	public void doExecute(TestContext context) {
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

	protected String getTestDirName(TestContext context) {
		return this.getClass().getSimpleName() + "-" + randomNum;
	}
	
	protected void addParameters(CoreParameters params, TestContext context) {
		params.setHostname(getVariable(context, PARAM_HOSTNAME));
		params.setUsername(getVariable(context, PARAM_OADMIN_USERNAME));
		params.setPassword(getVariable(context, PARAM_OADMIN_PASSWORD));
		params.setIgnoreAdminAccount(getIgnoreAdminAccount(context));
		params.setStage(getVariable(context, PARAM_STAGE));
		params.setProperties(new EnvironmentProperties(params.getStage(), null));
	}
	
	public String getVariable(TestContext context, String varname) {
		try {
			return context.getVariable(varname);
		} catch (Exception e) {}
		return null;
	}
	
	public Boolean getIgnoreAdminAccount(TestContext context) {
		try {
			return Boolean.parseBoolean(context.getVariable(PARAM_IGNORE_ADMIN_ACC));
		} catch (Exception e) {}
		return false;
	}

	public File getTestDirectory() {
		return testDirectory;
	}

	public int getRandomNum() {
		return randomNum;
	}
}
