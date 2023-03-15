package com.axway.apim.user.it;

import com.axway.apim.test.actions.TestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.users.UserApp;
import com.axway.apim.users.lib.params.UserExportParams;
import com.axway.apim.user.it.testActions.CLIAbstractExportTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

public class ExportUserTestAction extends CLIAbstractExportTestAction {

    private static Logger LOG = LoggerFactory.getLogger(ExportUserTestAction.class);

    public ExportUserTestAction(TestContext context) {
        super(context);
    }

    @Override
    public ExportResult runTest(TestContext context) {
        UserExportParams params = new UserExportParams();
        addParameters(params, context);
        params.setLoginName(getParamLoginName(context));
        UserApp app = new UserApp();
        LOG.info("Running " + app.getClass().getSimpleName() + " with params: " + params);
        ExportResult result = app.export(params);
        if (this.getExpectedReturnCode(context) != result.getRc()) {
            throw new ValidationException("Expected RC was: " + this.getExpectedReturnCode(context) + " but got: " + result.getRc());
        }
        return result;
    }

    private String getParamLoginName(TestContext context) {
        try {
            return context.getVariable(TestParams.PARAM_LOGINNAME);
        } catch (Exception ignore) {
        }
        return null;
    }
}
