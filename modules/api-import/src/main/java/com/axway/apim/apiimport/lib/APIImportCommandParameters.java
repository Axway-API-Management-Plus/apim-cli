package com.axway.apim.apiimport.lib;

import org.apache.commons.cli.CommandLine;

import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportCommandParameters extends CommandParameters {

	public APIImportCommandParameters(CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment)
			throws AppException {
		super(cmd, internalCmd, environment);
	}
	
	/*public boolean deleteLocalFolder() {
		if(getValue("deleteFolder")==null) return false;
		return Boolean.parseBoolean(getValue("deleteFolder"));
	}*/

}
