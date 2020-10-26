package com.axway.apim.setup.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.setup.APIManagerSetupApp;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.lib.testActions.CLIAbstractExportTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportManagerConfigTestAction extends CLIAbstractExportTestAction implements TestParams {

	private static Logger LOG = LoggerFactory.getLogger(ExportManagerConfigTestAction.class);
	
	public ExportManagerConfigTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public ExportResult runTest(TestContext context) {
		APIManagerSetupExportParams params = new APIManagerSetupExportParams();
		addParameters(params, context);
		params.setRemoteHostName(getVariable(context, PARAM_NAME));
		params.setRemoteHostId(getVariable(context, PARAM_ID));
		
		APIManagerSetupApp app = new APIManagerSetupApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ExportResult result = app.runExport(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
		return result;
	}
}
