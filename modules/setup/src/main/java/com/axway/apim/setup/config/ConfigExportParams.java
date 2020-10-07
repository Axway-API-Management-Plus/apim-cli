package com.axway.apim.setup.config;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class ConfigExportParams extends StandardExportParams {
	
	/** The name of the config field  */
	private String fieldName;

	public ConfigExportParams() {
		// TODO Auto-generated constructor stub
	}
	
	public static synchronized ConfigExportParams getInstance() {
		return (ConfigExportParams)CoreParameters.getInstance();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}	
}
