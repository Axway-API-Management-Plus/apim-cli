package com.axway.apim.apiimport;

import com.axway.apim.lib.errorHandling.AppException;

public class DesiredTestOnlyAPI extends DesiredAPI {

	public DesiredTestOnlyAPI() throws AppException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getApiDefinitionImport() {
		return "https://petstore.swagger.io/v2/swagger.json";
	}

	@Override
	public String getPath() {
		return "/just/a/path";
	}
}
