package com.axway.apim;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIImportConfig;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.IAPIDefinition;

/**
 * This is the Entry-Point of program and responsible to:</br>
 * - read the command-line parameters to create a <code>CommandParameters</code></br>
 * - next is to read the API-Contract by creating an <code>APIImportConfig</code> instance and calling getImportAPIDefinition()</br>
 * - the <code>APIManagerAdapter</code> method: <code>getAPIManagerAPI()</code> is used to create the API-Manager API state</br>
 * - An <code>APIChangeState</code> is created based on ImportAPI & API-Manager API
 * - Finally the APIManagerAdapter:applyChanges() is called to replicate the state into the APIManager.   
 * 
 * @author cwiechmann@axway.com
 */
public class App {

	private static Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String args[]) { 
		int rc = run(args);
		System.exit(rc);
	}
		
	public static int run(String args[]) {
		try {
			LOG.info("------------------------------------------------------------------------");
			LOG.info("API-Manager Promote Version: 1.3");
			LOG.info("                                                                        ");
			LOG.info("To report issues or get help, please visit: ");
			LOG.info("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote");
			LOG.info("------------------------------------------------------------------------");
			
			Options options = new Options();
			Option option;
			
			option = new Option("a", "swagger", true, "The Swagger-API Definition (JSON-Formated):\n"
					+ "- in local filesystem using a relativ or absolute path. Example: swagger_file.json\n"
					+ "- a URL providing the Swagger-File. Example: [username/password@]https://any.host.com/my/path/to/swagger.json\n"
					+ "- a file called anyname-i-want.url which contains a line with the URL (same format as above).");
				option.setRequired(true);
				option.setArgName("swagger_file.json");
			options.addOption(option);
			
			option = new Option("c", "contract", true, "This is the JSON-Formatted API-Config containing information how to expose the API");
				option.setRequired(true);
				option.setArgName("api_contract.json");
			options.addOption(option);
			
			option = new Option("s", "stage", true, "The stage this API should be imported.\n"
					+ "Will be used to lookup stage specific API-Contract overrides (e.g.: api_contract.preprod.json)");
				option.setArgName("preprod");
			options.addOption(option);
			
			option = new Option("h", "host", true, "The API-Manager hostname the API should be imported");
				option.setRequired(true);
				option.setArgName("api-host");
			options.addOption(option);
			
			option = new Option("u", "username", true, "Username used to authenticate. Please note, that this user must have Admin-Role");
				option.setRequired(true);
				option.setArgName("apiadmin");
			options.addOption(option);
			
			option = new Option("p", "password", true, "Password used to authenticate");
				option.setRequired(true);
				option.setArgName("changeme");
			options.addOption(option);
			
			option = new Option("f", "force", true, "Breaking changes can't be imported without this flag, unless the API is unpublished.");
				option.setArgName("true/[false]");
			options.addOption(option);
			
			option = new Option("iq", "ignoreQuotas", true, "Use this flag to ignore configured API quotas.");
			option.setArgName("true/[false]");
			options.addOption(option);
			
			option = new Option("io", "ignoreClientOrgs", true, "Use this flag to ignore configured Client-Organizations.");
			option.setArgName("true/[false]");
			options.addOption(option);
			
			option = new Option("ia", "ignoreClientApps", true, "Use this flag to ignore configured Client-Applications.");
			option.setArgName("true/[false]");
			options.addOption(option);
			
			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(140);
			CommandLine cmd = null;
			String scriptExt = "sh";
			if(System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = "bat";
			try {
				cmd = parser.parse( options, args, false);
			} catch (ParseException e) {
				formatter.printHelp("Swagger-Import", options, true);
				System.out.println("\n");
				System.out.println("ERROR: " + e.getMessage());
				System.out.println();
				System.out.println("You may run one of the following examples:");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a samples/petstore.json -c samples/minimal-config.json -h localhost -u apiadmin -p changeme");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a samples/petstore.json -c samples/minimal-config.json -h localhost -u apiadmin -p changeme -s prod");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a samples/petstore.json -c samples/complete-config.json -h localhost -u apiadmin -p changeme");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a samples/petstore.json -c samples/org-and-apps-config.json -h localhost -u apiadmin -p changeme");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a samples/petstore.url -c samples/minimal-config.json -h localhost -u apiadmin -p changeme");
				System.out.println("scripts"+File.separator+"run-swagger-import."+scriptExt+" -a https://petstore.swagger.io/v2/swagger.json -c samples/minimal-config.json -h localhost -u apiadmin -p changeme");				
				System.out.println();
				System.out.println("For more information visit: https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki");
				
				System.exit(99);
			}
			
			CommandParameters params = CommandParameters.getInstance();
			params.setCmd(cmd);
			
			APIManagerAdapter apimAdapter = new APIManagerAdapter();
			
			APIImportConfig contract = new APIImportConfig(params.getOptionValue("contract"), params.getOptionValue("stage"), params.getOptionValue("swagger"));
			IAPIDefinition desiredAPI = contract.getImportAPIDefinition();
			IAPIDefinition actualAPI = APIManagerAdapter.getAPIManagerAPI(APIManagerAdapter.getExistingAPI(desiredAPI.getPath()), desiredAPI);
			APIChangeState changeActions = new APIChangeState(actualAPI, desiredAPI);			
			
			apimAdapter.applyChanges(changeActions);
			LOG.info("Successfully replicated API-State into API-Manager");
			return 0;
		} catch (AppException ap) {
			if(ap.isLogStackStrace()) {
				LOG.error(ap.getMessage(), ap);
			} else {
				LOG.warn(ap.getMessage());
			}
			return ap.getErrorCode().getCode();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
}
