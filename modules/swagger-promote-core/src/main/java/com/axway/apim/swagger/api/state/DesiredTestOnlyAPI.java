package com.axway.apim.swagger.api.state;

import com.axway.apim.lib.AppException;

public class DesiredTestOnlyAPI extends DesiredAPI implements IAPI {

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
