package com.axway.apim.setup.remoteHosts.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.setup.APIManagerRemoteHostApp;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;
import com.axway.lib.testActions.CLIAbstractExportTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportRemoteHostsTestAction extends CLIAbstractExportTestAction implements TestParams {

	private static Logger LOG = LoggerFactory.getLogger(ExportRemoteHostsTestAction.class);
	
	public ExportRemoteHostsTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public ExportResult runTest(TestContext context) {
		RemoteHostsExportParams params = new RemoteHostsExportParams();
		addParameters(params, context);
		params.setName(context.getVariable(PARAM_NAME));
		
		APIManagerRemoteHostApp app = new APIManagerRemoteHostApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ExportResult result = app.exportRemoteHosts(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
		return result;
	}
}
