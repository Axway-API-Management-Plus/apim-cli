package com.axway.apim.setup.config.it;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.setup.APIManagerConfigApp;
import com.axway.lib.testActions.CLIAbstractImportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ImportConfigTestAction extends CLIAbstractImportTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(ImportConfigTestAction.class);
	
	@Override
	public void runTest(TestContext context) {
		StandardImportParams params = new StandardImportParams();
		addCoreParameter(params, context);
		params.setConfig(this.configFile.getPath());
		
		LOG.info("Running APIManagerConfigApp with params: "+params);
		
		APIManagerConfigApp app = new APIManagerConfigApp();
		int rc = app.importConfig(params);
		if(this.getExpectedReturnCode(context)!=rc) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + rc);
		}
	}

	@Override
	protected String getTestDirName(TestContext context) {
		int randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
		return this.getClass().getSimpleName() + "-" + randomNum;
	}
}
