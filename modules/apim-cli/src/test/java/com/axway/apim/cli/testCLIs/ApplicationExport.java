package com.axway.apim.cli.testCLIs;

import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;

public class ApplicationExport implements APIMCLIServiceProvider {


	@Override
	public String getName() {
		return "Export applications";
	}

	@Override
	public String getVersion() {
		return ApplicationExport.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "app";
	}

	@Override
	public String getGroupDescription() {
		return "Manage your applications";
	}

	@CLIServiceMethod(name = "export", description = "Export applications from the API-Manager")
	public int export(String[] args) {
		return 0;
	}



}
