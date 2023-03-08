package com.axway.apim.setup.it.testActions;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.context.TestContext;

import java.io.File;

public abstract class CLIAbstractExportTestAction extends CLIAbstractTestAction  {

	protected ExportResult lastResult;

	public CLIAbstractExportTestAction(TestContext context) {
		super(context);
	}

	@Override
	public void doExecute(TestContext context, File testDirectory) {
		this.lastResult = runTest(context);
	}

	@Override
	protected void addParameters(CoreParameters params, TestContext context) {
		super.addParameters(params, context);
		((StandardExportParams)params).setTarget(getVariable(context, TestParams.PARAM_TARGET));
		((StandardExportParams)params).setOutputFormat(OutputFormat.valueOf(getVariable(context, TestParams.PARAM_OUTPUT_FORMAT)));
	}

	public abstract ExportResult runTest(TestContext context);

	public ExportResult getLastResult() {
		return lastResult;
	}
}
