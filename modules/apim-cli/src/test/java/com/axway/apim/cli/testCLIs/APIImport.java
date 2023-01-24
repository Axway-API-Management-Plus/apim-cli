package com.axway.apim.cli.testCLIs;

import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;

/**
 * This is the Entry-Point of program and responsible to:  
 * - read the command-line parameters to create a <code>CommandParameters</code>
 * - next is to read the API-Contract by creating an <code>APIImportConfig</code> instance and calling getImportAPIDefinition()
 * - the <code>APIManagerAdapter</code> method: <code>getAPIManagerAPI()</code> is used to create the API-Manager API state
 * - An <code>APIChangeState</code> is created based on ImportAPI and API-Manager API
 * - Finally the APIManagerAdapter:applyChanges() is called to replicate the state into the APIManager.   
 * 
 * @author cwiechmann@axway.com
 */
public class APIImport implements APIMCLIServiceProvider {

	public static void main(String[] args) {
		int rc = importAPI(args);
		System.exit(rc);
	}
	
	@CLIServiceMethod(name = "import", description = "Import APIs into the API-Manager")
	public static int importAPI(String[] args) {
		return 0;
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
		return APIImport.class.getPackage().getImplementationVersion();
	}
	
	public String getName() {
		return "API - I M P O R T";
	}
}
