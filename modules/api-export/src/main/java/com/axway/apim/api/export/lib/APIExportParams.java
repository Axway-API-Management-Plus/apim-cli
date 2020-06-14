package com.axway.apim.api.export.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class APIExportParams extends StandardExportParams {

	public APIExportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized APIExportParams getInstance() {
		return (APIExportParams)CommandParameters.getInstance();
	}
	
	public String getAPIName() {
		return getValue("name");
	}
	
	public String getAPIId() {
		return getValue("id");
	}
	
	public boolean deleteLocalFolder() {
		if(getValue("deleteFolder")==null) return false;
		return Boolean.parseBoolean(getValue("deleteFolder"));
	}
	
	public String getLocalFolder() {
		return (getValue("localFolder")==null) ? "." : getValue("localFolder");
	}
}
