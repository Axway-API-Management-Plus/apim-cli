package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

import com.axway.apim.lib.errorHandling.AppException;

public class StandardExportParams extends CommandParameters {
	
	public static enum Wide {
		standard, 
		wide, 
		ultra
	}
	
	public static enum OutputFormat {
		console, 
		json
	}

	public StandardExportParams(CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment)
			throws AppException {
		super(cmd, internalCmd, environment);
	}
	
	public static synchronized StandardExportParams getInstance() {
		return (StandardExportParams)CommandParameters.getInstance();
	}
	
	public Wide getWide() {
		try {
			if(hasOption("wide")) {
				return Wide.wide;
			} 
			if(hasOption("ultra")) {
				return Wide.ultra;
			}
		} catch (Exception ignore) {}
		return Wide.standard;
	}
	
	public OutputFormat getOutputFormat() {
		try {
			return OutputFormat.valueOf(getValue("output"));
		} catch (Exception e) {
			return OutputFormat.console;
		}
	}
	
	public boolean deleteLocalFolder() {
		if(hasOption("deleteFolder")) return true;
		if(getValue("deleteFolder")==null) return false;
		return Boolean.parseBoolean(getValue("deleteFolder"));
	}
}