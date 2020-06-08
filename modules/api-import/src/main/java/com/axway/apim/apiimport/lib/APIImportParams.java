package com.axway.apim.apiimport.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportParams extends CommandParameters {

	public APIImportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized APIImportParams getInstance() {
		return (APIImportParams)CommandParameters.getInstance();
	}

	@Override
	public boolean ignoreCache() {
		// For import action we ignore the cache in all cases!
		return true;
	}
}
