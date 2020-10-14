package com.axway.apim.setup.remoteHosts.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.setup.APIManagerRemoteHostApp;
import com.axway.lib.testActions.CLIAbstractImportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ImportRemoteHostsTestAction extends CLIAbstractImportTestAction {

	private static Logger LOG = LoggerFactory.getLogger(ImportRemoteHostsTestAction.class);
	
	public ImportRemoteHostsTestAction(TestContext context) {
		super(context);
	}

	@Override
	public void runTest(TestContext context) {
		StandardImportParams params = new StandardImportParams();
		addParameters(params, context);
		params.setConfig(this.configFile.getPath());
		
		APIManagerRemoteHostApp app = new APIManagerRemoteHostApp();
		
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ImportResult result = app.importRemoteHosts(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
	}
}
