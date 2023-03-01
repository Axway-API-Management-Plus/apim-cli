package com.axway.apim.appimport.it;

import com.axway.apim.test.actions.TestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.appexport.ApplicationExportApp;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.test.actions.CLIAbstractExportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportAppTestAction extends CLIAbstractExportTestAction implements TestParams {

	private static Logger LOG = LoggerFactory.getLogger(ExportAppTestAction.class);
	
	public ExportAppTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public ExportResult runTest(TestContext context) {
		AppExportParams params = new AppExportParams();
		addParameters(params, context);
		params.setName(getVariable(context, PARAM_NAME));
		
		ApplicationExportApp app = new ApplicationExportApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ExportResult result = app.export(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
		return result;
	}
}
