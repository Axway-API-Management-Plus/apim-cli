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
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SwaggerImportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(SwaggerImportTestAction.class);
	
	//private String swaggerFile;
	
	//private String configFile;
	
	@Override
	public void doExecute(TestContext context) {
		String origSwaggerFile 			= context.getVariable("swaggerFile");
		String origConfigFile 			= context.getVariable("configFile");
		String stage				= null;
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};
		String swaggerFile = replaceDynamicContentInFile(origSwaggerFile, context);
		String configFile = replaceDynamicContentInFile(origConfigFile, context);
		LOG.info("Using Replaced Swagger-File: " + swaggerFile);
		LOG.info("Using Replaced configFile-File: " + configFile);
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		String enforce = "true";
		String ignoreQuotas = "false";
		String ignoreClientOrgs = "false";
		
		try {
			enforce = context.getVariable("enforce");
		} catch (Exception ignore) {};
		try {
			ignoreClientOrgs = context.getVariable("ignoreQuotas");
		} catch (Exception ignore) {};
		try {
			ignoreClientOrgs = context.getVariable("ignoreClientOrgs");
		} catch (Exception ignore) {};
		
		if(stage==null) {
			stage = "NOT_SET";
		} else {
			// We need to prepare the dynamic staging file used during the test.
			String stageConfigFile = origConfigFile.substring(0, origConfigFile.lastIndexOf(".")+1) + stage + origConfigFile.substring(origConfigFile.lastIndexOf("."));
			// This creates the dynamic staging config file! (Fort testing, we also support reading out of a file directly)
			stage = replaceDynamicContentInFile(stageConfigFile, context);
		}

		String[] args = new String[] { 
				"-a", swaggerFile, 
				"-c", configFile, 
				"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
				"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
				"-u", context.replaceDynamicContentInString("${apiManagerUser}"),
				"-s", stage, 
				"-f", enforce, 
				"-iq", ignoreQuotas, 
				"-io", ignoreClientOrgs};
		
		int rc = App.run(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 */
	private String replaceDynamicContentInFile(String pathToFile, TestContext context) {
		ObjectMapper mapper = new ObjectMapper();
		
		File inputFile = new File(pathToFile);
		InputStream is = null;
		OutputStream os = null;
		try {
			if(inputFile.exists()) { 
				is = new FileInputStream(pathToFile);
			} else {
				is = this.getClass().getResourceAsStream(pathToFile);
			}
			if(is == null) {
				throw new IOException("Unable to read swagger file from: " + pathToFile);
			}
			String jsonData = IOUtils.toString(is);
			String filename = pathToFile.substring(pathToFile.lastIndexOf("/")+1); // e.g.: petstore.json, no-change-xyz-config.<stage>.json, 
			String prefix = filename.substring(0, filename.indexOf("."));
			String suffix = filename.substring(filename.indexOf("."));
			String jsonReplaced = context.replaceDynamicContentInString(jsonData);
			File tempFile = File.createTempFile(prefix, suffix);
			os = new FileOutputStream(tempFile);
			IOUtils.write(jsonReplaced, os);
			tempFile.deleteOnExit();
			return tempFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(os!=null)
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}
}
