package com.axway.apim.user.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ImportResult;
import com.axway.apim.users.UserApp;
import com.axway.apim.users.lib.UserImportParams;
import com.axway.apim.user.it.testActions.CLIAbstractImportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ImportUserTestAction extends CLIAbstractImportTestAction {

	private static final Logger LOG = LoggerFactory.getLogger(ImportUserTestAction.class);

	public ImportUserTestAction(TestContext context) {
		super(context);
	}

	@Override
	public void runTest(TestContext context) {
		UserImportParams params = new UserImportParams();
		addParameters(params, context);
		params.setConfig(this.configFile.getPath());
		UserApp app = new UserApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		ImportResult result = app.importUsers(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
	}
}
