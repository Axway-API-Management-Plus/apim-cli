package com.axway.apim.setup.it.testActions;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CLIAbstractTestAction extends AbstractTestAction {

    protected static Logger LOG = LoggerFactory.getLogger(CLIAbstractTestAction.class);

    protected File testDirectory;

    public TestContext context;

    public int randomNum;

    public CLIAbstractTestAction(TestContext context) {
        super();
        this.context = context;
        this.randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
        this.testDirectory = createTestDirectory();
    }

    @Override
    public void doExecute(TestContext context) {
        doExecute(context, testDirectory);
    }

    public abstract void doExecute(TestContext context, File testDirectory);

    protected int getExpectedReturnCode(TestContext context) {
        int expectedReturnCode = 0;
        try {
            expectedReturnCode = Integer.parseInt(context.getVariable(TestParams.PARAM_EXPECTED_RC));
        } catch (Exception ignore) {
        }
        return expectedReturnCode;
    }

    protected File createTestDirectory() {
        String testDirName = getTestDirName();
        String tmpDir = System.getProperty("java.io.tmpdir");
        File testDir = new File(tmpDir + File.separator + testDirName);
        if (testDir.mkdir()) {
            LOG.info("Successfully created Test-Directory: " + testDir);
        } else {
            throw new ValidationException("Error creating test directory: " + testDir);
        }
        return testDir;
    }

    protected String getTestDirName() {
        return this.getClass().getSimpleName() + "-" + randomNum;
    }

    protected void addParameters(CoreParameters params, TestContext context) {
        params.setHostname(getVariable(context, TestParams.PARAM_HOSTNAME));
        boolean useApiAdmin = Boolean.parseBoolean(getVariable(context, "useApiAdmin"));
        if (useApiAdmin) {
            params.setUsername(getVariable(context, "apiManagerUser"));
            params.setPassword(getVariable(context, "apiManagerPass"));
        } else {
            params.setUsername(getVariable(context, TestParams.PARAM_OADMIN_USERNAME));
            params.setPassword(getVariable(context, TestParams.PARAM_OADMIN_PASSWORD));
        }
        params.setStage(getVariable(context, TestParams.PARAM_STAGE));
        params.setProperties(new EnvironmentProperties(params.getStage(), null));
    }

    public String getVariable(TestContext context, String varname) {
        try {
            return context.getVariable(varname);
        } catch (Exception e) {
            LOG.error("Error reading variable : {}", varname);
        }
        return null;
    }

    public File getTestDirectory() {
        return testDirectory;
    }

    public int getRandomNum() {
        return randomNum;
    }
}
