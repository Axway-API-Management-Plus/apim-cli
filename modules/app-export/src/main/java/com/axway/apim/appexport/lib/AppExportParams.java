package com.axway.apim.appexport.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class AppExportParams extends StandardExportParams {

	public AppExportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized AppExportParams getInstance() {
		return (AppExportParams)CommandParameters.getInstance();
	}
	
	public String getAppState() {
		return getValue("state");
	}
	
	public String getAppName() {
		return getValue("name");
	}
	
	public String getAppId() {
		return getValue("id");
	}
	
	public String getOrgName() {
		return getValue("orgName");
	}
	
	public String getLocalFolder() {
		return (getValue("localFolder")==null) ? "." : getValue("localFolder");
	}
}
