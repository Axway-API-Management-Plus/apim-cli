package com.axway.apim.setup.config.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.setup.APIManagerConfigApp;
import com.axway.apim.setup.config.lib.ConfigExportParams;
import com.axway.lib.testActions.CLIAbstractExportTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportConfigTestAction extends CLIAbstractExportTestAction implements TestParams {

	private static Logger LOG = LoggerFactory.getLogger(ExportConfigTestAction.class);
	
	public ExportConfigTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public ExportResult runTest(TestContext context) {
		ConfigExportParams params = new ConfigExportParams();
		addParameters(params, context);
		params.setTarget(context.getVariable(PARAM_TARGET));
		params.setOutputFormat(OutputFormat.valueOf(context.getVariable(PARAM_OUTPUT_FORMAT)));
		
		APIManagerConfigApp app = new APIManagerConfigApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ExportResult result = app.exportConfig(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
		return result;
	}
}
