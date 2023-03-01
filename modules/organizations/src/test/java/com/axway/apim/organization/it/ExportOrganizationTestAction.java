package com.axway.apim.organization.it;

import com.axway.apim.test.actions.TestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.organization.OrganizationApp;
import com.axway.apim.organization.lib.OrgExportParams;
import com.axway.apim.organization.it.testActions.CLIAbstractExportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportOrganizationTestAction extends CLIAbstractExportTestAction implements TestParams {

	private static Logger LOG = LoggerFactory.getLogger(ExportOrganizationTestAction.class);
	
	public ExportOrganizationTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public ExportResult runTest(TestContext context) {
		OrgExportParams params = new OrgExportParams();
		addParameters(params, context);
		params.setName(getVariable(context, PARAM_NAME));
		
		OrganizationApp app = new OrganizationApp();
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		ExportResult result = app.exportOrgs(params);
		if(this.getExpectedReturnCode(context)!=result.getRc()) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
		}
		return result;
	}
}
