package com.axway.apim.setup.it;

import com.axway.apim.setup.it.testActions.CLIAbstractExportTestAction;
import com.axway.apim.test.actions.TestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.setup.APIManagerSettingsApp;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportManagerConfigTestAction extends CLIAbstractExportTestAction {

    private static Logger LOG = LoggerFactory.getLogger(ExportManagerConfigTestAction.class);

    public ExportManagerConfigTestAction(TestContext context) {
        super(context);
    }

    @Override
    public ExportResult runTest(TestContext context) {
        APIManagerSetupExportParams params = new APIManagerSetupExportParams();
        addParameters(params, context);
        params.setRemoteHostName(getVariable(context, TestParams.PARAM_NAME));
        params.setRemoteHostId(getVariable(context, TestParams.PARAM_ID));

        APIManagerSettingsApp app = new APIManagerSettingsApp();
        LOG.info("Running " + app.getClass().getSimpleName() + " with params: " + params);

        ExportResult result = app.runExport(params);
        if (this.getExpectedReturnCode(context) != result.getRc()) {
            throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
        }
        return result;
    }
}
