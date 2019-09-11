package com.axway.apim.test.basic;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.AbstractAPI;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.axway.apim.swagger.api.state.IAPI;

public class DesiredTestAPI extends DesiredAPI implements IAPI {

	public DesiredTestAPI() throws AppException {
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
