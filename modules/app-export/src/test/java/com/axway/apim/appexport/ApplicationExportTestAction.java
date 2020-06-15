package com.axway.apim.appexport;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class ApplicationExportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(ApplicationExportTestAction.class);

	@Override
	public void doExecute(TestContext context) {
		
		boolean useEnvironmentOnly	= false;
		boolean ignoreAdminAccount	= false;
		String stage				= null;
		String orgNameFilter		= null;
		String stateFilter			= null;
		
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};
		
		try {
			orgNameFilter 				= context.getVariable("orgNameFilter");
		} catch (CitrusRuntimeException ignore) {};
		
		try {
			stateFilter 				= context.getVariable("stateFilter");
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
		
		if(stage==null) {
			stage = "NOT_SET";
		}

		List<String> args = new ArrayList<String>();
		if(useEnvironmentOnly) {
			args.add("-n");
			args.add(context.replaceDynamicContentInString("${appName}"));
			args.add("-s");
			args.add(stage);
		} else {
			args.add("-n");
			args.add(context.replaceDynamicContentInString("${appName}"));
			args.add("-l");
			args.add(context.replaceDynamicContentInString("${localFolder}"));
			args.add("-h");
			args.add(context.replaceDynamicContentInString("${apiManagerHost}"));
			args.add("-p");
			args.add(context.replaceDynamicContentInString("${apiManagerPass}"));
			args.add("-u");
			args.add(context.replaceDynamicContentInString("${apiManagerUser}"));
			args.add("-s");
			args.add(stage);
			if(orgNameFilter!=null) {
				args.add("-orgName");
				args.add(orgNameFilter);
			}
			if(stateFilter!=null) {
				args.add("-state");
				args.add(stateFilter);
			}
			if(ignoreAdminAccount) {
				args.add("-ignoreAdminAccount");
			}
		}
		int rc = ApplicationExportApp.export(args.toArray(new String[args.size()]));
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}

}
