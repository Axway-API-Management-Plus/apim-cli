package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;

public class ExportCommandParameters extends CommandParameters {

	public ExportCommandParameters(CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment)
			throws AppException {
		super(cmd, internalCmd, environment);
	}
	
	public boolean deleteLocalFolder() {
		if(getValue("deleteFolder")==null) return false;
		return Boolean.parseBoolean(getValue("deleteFolder"));
	}

}
