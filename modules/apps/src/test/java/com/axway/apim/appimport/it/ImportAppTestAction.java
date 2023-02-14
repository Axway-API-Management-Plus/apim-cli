package com.axway.apim.appimport.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.appimport.ClientApplicationImportApp;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.testActions.CLIAbstractImportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ImportAppTestAction extends CLIAbstractImportTestAction {

	private static Logger LOG = LoggerFactory.getLogger(ImportAppTestAction.class);
	
	public ImportAppTestAction(TestContext context) {
		super(context);
	}

	@Override
	public void runTest(TestContext context) {
		AppImportParams params = new AppImportParams();
		addParameters(params, context);
		params.setConfig(this.configFile.getPath());
		
		ClientApplicationImportApp app = new ClientApplicationImportApp();
		
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ImportResult result = app.importApp(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
	}
}
