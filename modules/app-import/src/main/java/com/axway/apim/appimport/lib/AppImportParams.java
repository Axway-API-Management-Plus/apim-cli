package com.axway.apim.appimport.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class AppImportParams extends CommandParameters {

	public AppImportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized AppImportParams getInstance() {
		return (AppImportParams)CommandParameters.getInstance();
	}
	
	@Override
	public boolean ignoreCache() {
		// For import action we ignore the cache in all cases!
		return false;
	}
}
