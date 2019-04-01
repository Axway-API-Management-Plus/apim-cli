package com.axway.apim.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.App;
import com.axway.apim.lib.CommandParameters;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class WSDLImportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(WSDLImportTestAction.class);
	
	//private String swaggerFile;
	
	//private String configFile;
	
	@Override
	public void doExecute(TestContext context) {
		String wsdlURL 			= context.getVariable("wsdlURL");
		String configFile 			= context.getVariable("configFile");
		String stage				= null;

		//String configFile = replaceDynamicContentInFile(origConfigFile, context);
		LOG.info("Using WSDL-URL: " + wsdlURL);
		LOG.info("Using configFile-File: " + configFile);
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		String enforce = "false";
		String ignoreQuotas = "false";
		String clientOrgsMode = CommandParameters.MODE_REPLACE;
		String clientAppsMode = CommandParameters.MODE_REPLACE;;
		
		try {
			enforce = context.getVariable("enforce");
		} catch (Exception ignore) {};
		try {
			ignoreQuotas = context.getVariable("ignoreQuotas");
		} catch (Exception ignore) {};
		try {
			clientOrgsMode = context.getVariable("clientOrgsMode");
		} catch (Exception ignore) {};
		try {
			clientAppsMode = context.getVariable("clientAppsMode");
		} catch (Exception ignore) {};
		
		if(stage==null) {
			stage = "NOT_SET";
		} else {
			// We need to prepare the dynamic staging file used during the test.
			//String stageConfigFile = origConfigFile.substring(0, origConfigFile.lastIndexOf(".")+1) + stage + origConfigFile.substring(origConfigFile.lastIndexOf("."));
			// This creates the dynamic staging config file! (Fort testing, we also support reading out of a file directly)
			//stage = replaceDynamicContentInFile(stageConfigFile, context);
		}

		String[] args = new String[] { 
				"-w", wsdlURL, 
				"-c", configFile, 
				"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
				"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
				"-u", context.replaceDynamicContentInString("${apiManagerUser}"),
				"-s", stage, 
				"-f", enforce, 
				"-iq", ignoreQuotas, 
				"-clientOrgsMode", clientOrgsMode, 
				"-clientAppsMode", clientAppsMode};
		
		int rc = App.run(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
	
}
