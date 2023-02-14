package com.axway.apim.organization.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.organization.OrganizationApp;
import com.axway.apim.organization.lib.OrgImportParams;
import com.axway.apim.organization.it.testActions.CLIAbstractImportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ImportOrganizationTestAction extends CLIAbstractImportTestAction {

	private static Logger LOG = LoggerFactory.getLogger(ImportOrganizationTestAction.class);
	
	public ImportOrganizationTestAction(TestContext context) {
		super(context);
	}

	@Override
	public void runTest(TestContext context) {
		OrgImportParams params = new OrgImportParams();
		addParameters(params, context);
		params.setConfig(this.configFile.getPath());
		
		OrganizationApp app = new OrganizationApp();
		
		LOG.info("Running "+app.getClass().getSimpleName()+" with params: "+params);
		
		int rc = app.importOrganization(params);
		if(this.getExpectedReturnCode(context)!=rc) {
			throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + rc);
		}
	}
}
