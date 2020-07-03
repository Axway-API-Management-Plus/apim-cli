package com.axway.apim.orgexport.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class OrgExportParams extends StandardExportParams {

	public OrgExportParams(APIMCoreCLIOptions parser)
			throws AppException {
		super(parser.getCmd(), parser.getInternalCmd(), new EnvironmentProperties(parser.getCmd().getOptionValue("stage"), parser.getCmd().getOptionValue("swaggerPromoteHome")));
	}
	
	public static synchronized OrgExportParams getInstance() {
		return (OrgExportParams)CommandParameters.getInstance();
	}
	
	public String getName() {
		return getValue("name");
	}
	
	public String getId() {
		return getValue("id");
	}
}
