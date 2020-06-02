package com.axway.apim.cli.testCLIs;

import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;

/**
 * 
 * @author cwiechmann@axway.com
 */
public class APIExport implements APIMCLIServiceProvider {

	public static void main(String args[]) { 
		int rc = export(args);
		System.exit(rc);
	}
	
	@CLIServiceMethod(description = "Export APIs from the API-Manager")
	public static int export(String args[]) {
		return 0;
	}
	
	@Override
	public String getName() {
		return "API Export";
	}

	@Override
	public String getGroupId() {
		return "api";
	}
	
	@Override
	public String getGroupDescription() {
		return "Manage your APIs";
	}
	
	@Override
	public String getVersion() {
		return APIExport.class.getPackage().getImplementationVersion();
	}
}
