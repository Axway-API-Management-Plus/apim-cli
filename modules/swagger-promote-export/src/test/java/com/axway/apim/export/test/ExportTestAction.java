package com.axway.apim.export.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.ExportApp;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class ExportTestAction extends AbstractTestAction {
	
	public static String EXPORT_API			= "exportApi";
	public static String EXPORT_LOCATION	= "exportLocation";
	
	
	private static Logger LOG = LoggerFactory.getLogger(ExportTestAction.class);
	
	@Override
	public void doExecute(TestContext context) {
		
		boolean useEnvironmentOnly	= false;
		
		String stage				= null;
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
		
		if(stage==null) {
			stage = "NOT_SET";
		} else {
			// We need to prepare the dynamic staging file used during the test.
		//	String stageConfigFile = origConfigFile.substring(0, origConfigFile.lastIndexOf(".")+1) + stage + origConfigFile.substring(origConfigFile.lastIndexOf("."));
		//	String replacedStagedConfig = configFile.substring(0, configFile.lastIndexOf("."))+"."+stage+".json";
			// This creates the dynamic staging config file! (For testing, we also support reading out of a file directly)
		//	replaceDynamicContentInFile(stageConfigFile, context, replacedStagedConfig);
		}
		String[] args;
		if(useEnvironmentOnly) {
			args = new String[] {  
					"-a", context.replaceDynamicContentInString("${exportApi}"), "-s", stage};
		} else {
			args = new String[] { 
					"-a", context.replaceDynamicContentInString("${exportApi}"), 
					"-l", context.replaceDynamicContentInString("${exportLocation}"), 
					"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
					"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
					"-u", context.replaceDynamicContentInString("${apiManagerUser}"),
					"-s", stage };
		}
		int rc = ExportApp.run(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
}
