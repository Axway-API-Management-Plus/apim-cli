package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

import com.axway.apim.lib.errorHandling.AppException;

public class StandardExportParams extends CommandParameters {
	
	public static enum Wide {
		standard, 
		wide, 
		ultra
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
			return Wide.valueOf(getValue("wide"));
		} catch (Exception e) {
			return Wide.standard;
		}
	}
}