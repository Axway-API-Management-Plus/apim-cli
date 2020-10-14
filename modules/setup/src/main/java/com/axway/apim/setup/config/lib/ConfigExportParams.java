package com.axway.apim.setup.config.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class ConfigExportParams extends StandardExportParams {

	public ConfigExportParams() {
	}
	
	public static synchronized ConfigExportParams getInstance() {
		return (ConfigExportParams)CoreParameters.getInstance();
	}
}
