package com.axway.apim.organization.it.testActions;

import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class CLIAbstractImportTestAction extends CLIAbstractTestAction implements TestParams {

	protected File configFile;
	
	protected File srcConfigFileName;

	public CLIAbstractImportTestAction(TestContext context) {
		super(context);
	}
	
	@Override
	public void doExecute(TestContext context, File testDirectory) {
		this.srcConfigFileName = getConfigFile(context);
		this.configFile = createTestConfig(srcConfigFileName, context, testDirectory);
		copyTestAssets(srcConfigFileName.getParentFile(), testDirectory);
		runTest(context);
	}
	
	public abstract void runTest(TestContext context);
	
	
	protected File getConfigFile(TestContext context) {
		String configFileName = context.getVariable(PARAM_CONFIGFILE);
		File configFile = new File(configFileName);
		if(!configFile.exists()) {
			URL filename = this.getClass().getResource(configFileName);
			if(filename==null) {
				throw new ValidationException("Configuration file : "+configFileName+" not found.");
			}
			configFile = new File(filename.getFile());
		}
		return configFile;
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 */
	private File createTestConfig(File configFile, TestContext context, File testDirectory) {
		OutputStream os = null;
		try {
			String baseFileName = configFile.getName();
			String config = IOUtils.toString(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8);
			String replacedConfig = context.replaceDynamicContentInString(config);
			
			configFile = new File(testDirectory, baseFileName);
			configFile.createNewFile();
			
			os = Files.newOutputStream(configFile.toPath());
			IOUtils.write(replacedConfig, os, StandardCharsets.UTF_8);
			LOG.info("Successfully created test configuration file: " + configFile);
			return configFile;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if(os!=null)
				try {
					os.close();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
		}
		throw new ValidationException("Unable to create test config file.");
	}
	
	private void copyTestAssets(File sourceDir, File testDir) {
		if(!sourceDir.exists()) {
			throw new ValidationException("Unable to copy test assets to test directory: '"+testDir+"'. Could not find sourceDir: '"+sourceDir+"'");
		}
		FileFilter filter = new WildcardFileFilter("*.crt", "*.jpg", "*.png", "*.pem");
		try {
			LOG.info("Copy *.crt, *.jpg, *.png, *.pem from source: "+sourceDir+" into test-dir: '"+testDir+"'");
			FileUtils.copyDirectory(sourceDir, testDir, filter, true);
		} catch (IOException e) {
			LOG.error("Unable to copy test assets from source: '"+sourceDir+"' into test directory: '"+testDir+"'", e);
		}
	}
}
