package com.axway.apim.appexport;

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
		String ignoreAdminAccount	= "false";
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
			ignoreAdminAccount = context.getVariable("ignoreAdminAccount");
		} catch (Exception ignore) {};
		
		if(stage==null) {
			stage = "NOT_SET";
		}
		if(vhostToExport==null) {
			vhostToExport = "NOT_SET";
		}
		String[] args;
		if(useEnvironmentOnly) {
			args = new String[] {  
					"-n", context.replaceDynamicContentInString("${appName}"), "-s", stage};
		} else {
			args = new String[] { 
					"-n", context.replaceDynamicContentInString("${appName}"),
					"-t", context.replaceDynamicContentInString("${targetFolder}"), 
					"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
					"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
					"-u", context.replaceDynamicContentInString("${apiManagerUser}"),
					"-s", stage,  
					"-ignoreAdminAccount", ignoreAdminAccount};
		}
		int rc = ApplicationExportApp.export(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}

}
