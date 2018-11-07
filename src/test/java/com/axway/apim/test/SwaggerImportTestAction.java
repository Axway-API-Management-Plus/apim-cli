package com.axway.apim.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.App;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SwaggerImportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(SwaggerImportTestAction.class);
	
	//private String swaggerFile;
	
	//private String configFile;
	
	@Override
	public void doExecute(TestContext context) {
		String swaggerFile 			= context.getVariable("swaggerFile");
		String configFile 			= context.getVariable("configFile");
		swaggerFile = replaceDynamicContentInFile(swaggerFile, context);
		configFile = replaceDynamicContentInFile(configFile, context);
		LOG.info("Using Replaced Swagger-File: " + swaggerFile);
		LOG.info("Using Replaced configFile-File: " + configFile);
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		String enforce = "true";
		try {
			enforce = context.getVariable("enforce");
		} catch (Exception ignore) {};
		System.out.println("Expected RC: " + expectedReturnCode);
		String[] args = new String[] { 
				"-a", swaggerFile, 
				"-c", configFile, 
				"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
				"-p", context.replaceDynamicContentInString("${apiManagerPass}"), 
				"-u", context.replaceDynamicContentInString("${apiManagerUser}"), 
				"-f", enforce};
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
			String jsonReplaced = context.replaceDynamicContentInString(jsonData);
			File tempFile = File.createTempFile("pathToFile", ".json");
			os = new FileOutputStream(tempFile);
			IOUtils.write(jsonReplaced, os);
			//tempSwaggerFile.deleteOnExit();
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
