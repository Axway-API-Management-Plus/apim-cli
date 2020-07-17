package com.axway.apim.api.export.lib;

import com.axway.apim.lib.APIMCoreCLIOptions;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class APIChangeParams extends APIExportParams {
	
	public APIChangeParams(APIMCoreCLIOptions parser) throws AppException {
		super(parser);
	}

	public static synchronized APIChangeParams getInstance() {
		return (APIChangeParams)CommandParameters.getInstance();
	}
	
	public String getNewBackend() {
		return getValue("newBackend");
	}
	
	public String getOldBackend() {
		return getValue("oldBackend");
	}
}
