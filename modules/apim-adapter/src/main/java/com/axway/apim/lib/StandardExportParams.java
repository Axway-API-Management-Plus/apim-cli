package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

import com.axway.apim.lib.errorHandling.AppException;

public class StandardExportParams extends CommandParameters {
	
	public static enum Wide {
		standard, 
		wide, 
		ultra
	}
	
	public static enum exportFormat {
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
	
	public exportFormat getExportFormat() {
		try {
			return exportFormat.valueOf(getValue("format"));
		} catch (Exception e) {
			return exportFormat.console;
		}
	}
}