package com.axway.apim.export.test;

import com.axway.apim.ExportApp;
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
		
		try {
			vhostToExport = context.getVariable("vhostToExport");
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
					"-a", context.replaceDynamicContentInString("${exportApi}"), "-s", stage};
		} else {
			args = new String[] { 
					"-a", context.replaceDynamicContentInString("${exportApi}"),
					"-v", vhostToExport,
					"-l", context.replaceDynamicContentInString("${exportLocation}"), 
					"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
					"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
					"-u", context.replaceDynamicContentInString("${apiManagerUser}"),
					"-s", stage,  
					"-ignoreAdminAccount", ignoreAdminAccount};
		}
		int rc = ExportApp.run(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
}
