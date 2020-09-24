package com.axway.apim.export.test;

import java.util.ArrayList;
import java.util.List;

import com.axway.apim.APIExportApp;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class ExportTestAction extends AbstractTestAction {
	
	public static String EXPORT_API			= "exportApi";
	public static String EXPORT_LOCATION	= "exportLocation";
	
	@Override
	public void doExecute(TestContext context) {
		
		boolean useEnvironmentOnly	= false;
		boolean ignoreAdminAccount	= false;
		String stage				= null;
		String vhostToExport		= null;
		
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};
		
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		try {
			useEnvironmentOnly 	= Boolean.parseBoolean(context.getVariable("useEnvironmentOnly"));
		} catch (Exception ignore) {};
		
		try {
			ignoreAdminAccount = Boolean.parseBoolean(context.getVariable("ignoreAdminAccount"));
		} catch (Exception ignore) {};
		
		try {
			vhostToExport = context.getVariable("vhostToExport");
		} catch (Exception ignore) {};
		
		if(stage==null) {
			stage = "NOT_SET";
		}
		if(vhostToExport==null) {
			vhostToExport = "NOT_SET";
		}
		List<String> args = new ArrayList<String>();
		if(useEnvironmentOnly) {
			args.add("-a");
			args.add(context.replaceDynamicContentInString("${exportApi}"));
			args.add("-s");
			args.add(stage);
			args.add("-t");
			args.add(context.replaceDynamicContentInString("${exportLocation}"));
			args.add("-o");
			args.add("json");
		} else {
			args.add("-a");
			args.add(context.replaceDynamicContentInString("${exportApi}"));
			args.add("-v");
			args.add(vhostToExport);
			args.add("-t");
			args.add(context.replaceDynamicContentInString("${exportLocation}"));
			args.add("-h");
			args.add(context.replaceDynamicContentInString("${apiManagerHost}"));
			args.add("-u");
			args.add(context.replaceDynamicContentInString("${oadminUsername1}"));
			args.add("-p");
			args.add(context.replaceDynamicContentInString("${oadminPassword1}"));
			args.add("-s");
			args.add(stage);
			args.add("-o");
			args.add("json");
			if(ignoreAdminAccount) {
				args.add("-ignoreAdminAccount");
			}
		}
		int rc = APIExportApp.exportAPI(args.toArray(new String[args.size()]));
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
}
