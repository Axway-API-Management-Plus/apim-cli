package com.axway.apim.users.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class UserExportParams extends StandardExportParams {

	public UserExportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized UserExportParams getInstance() {
		return (UserExportParams)CommandParameters.getInstance();
	}
	
	public String getLoginName() {
		return getValue("loginName");
	}
	
	public String getName() {
		return getValue("name");
	}
	
	public String getEmail() {
		return getValue("email");
	}
	
	public String getId() {
		return getValue("id");
	}
	
	public String getOrg() {
		return getValue("org");
	}
	
	public String getType() {
		return getValue("type");
	}
	
	public String getRole() {
		return getValue("role");
	}
	
	public String getState() {
		return getValue("state");
	}
	
	public String isEnabled() {
		return getValue("enabled");
	}
}
