package com.axway.apim;

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
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.IAPIDefinition;

public class App {

	private static Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String args[]) { 
		int rc = run(args);
		System.exit(rc);
	}
		
	public static int run(String args[]) {
		try {
			Options options = new Options();
			Option option;
			
			option = new Option("a", "swagger", true, "The Swagger-API Definition (JSON-Formated) in local-filesystem");
				option.setRequired(true);
				option.setArgName("./swagger.xyz.json");
			options.addOption(option);
			
			option = new Option("c", "contract", true, "This is the JSON-Formatted API-Contract containing information how to expose the API");
				option.setRequired(true);
				option.setArgName("./api_contract.json");
			options.addOption(option);
			
			option = new Option("s", "stage", true, "The stage this API should be import. Will be used to lookup stage overrides.");
				option.setArgName("preprod");
			options.addOption(option);
			
			option = new Option("h", "host", true, "The API-Manager hostname the API should be imported");
				option.setRequired(true);
				option.setArgName("api-host");
			options.addOption(option);
			
			option = new Option("u", "username", true, "Username used to authenticate");
				option.setRequired(true);
				option.setArgName("apiadmin");
			options.addOption(option);
			
			option = new Option("p", "password", true, "Password used to authenticate");
				option.setRequired(true);
				option.setArgName("changeme");
			options.addOption(option);
			
			option = new Option("f", "force", true, "Force breaking changes to be imported potentially update existing APIs");
				option.setArgName("true/false");
			options.addOption(option);
			
			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(140);
			CommandLine cmd = null;
			try {
				cmd = parser.parse( options, args, false);
			} catch (ParseException e) {
				System.out.println("\n\n");
				System.out.println(e.getMessage());
				System.out.println("\n\n");
				formatter.printHelp("Swagger-Import", options, true);
				
				System.out.println("You may run one of the samples");
				System.out.println("scripts/run-swagger-import.sh -a samples/petstore.json -c samples/minimal-config.json -h localhost -u apiadmin -p changeme");
				
				System.exit(99);
			}
			
			CommandParameters params = CommandParameters.getInstance();
			params.setCmd(cmd);
			
			APIManagerAdapter apimAdapter = new APIManagerAdapter();
			
			APIImportConfig contract = new APIImportConfig(params.getOptionValue("contract"), params.getOptionValue("stage"), params.getOptionValue("swagger"));
			IAPIDefinition desiredAPI = contract.getImportAPIDefinition();
			// Create the API-Definition that represent what we want to have
			//                  IAPIDefinition desiredAPI = new APIImportDefinition(contract, params.getOptionValue("swagger"));
			// Create an API-Definition that reflects the same API in API-Manager (or indicated)
			IAPIDefinition actualAPI = APIManagerAdapter.getAPIManagerAPI(APIManagerAdapter.getExistingAPI(desiredAPI.getPath()), desiredAPI.getCustomProperties());
			/* Both API-Definitions can be compared
			 * - is the Change is breaking
			 *   - and if yes, do we have a new version number + new exposure Path?
			 *   - or is the Force-Flag set (which is may be used on the Dev-Stage to allow frequent updates)
			 * - is the Change is Non-Breaking
			 *   - we need to know, if only changeable properties (like status, description, ... later tags, custom-props)
			 *     have been changed (the API-Definition needs to know that)
			 *     - if yes, update the existing API-Entity in API-Manager, not creating a new
			 *   - if changes are desired not applicable to the existing API, we create a new 
			 */
			APIChangeState changeActions = new APIChangeState(actualAPI, desiredAPI);
	
			
			
			apimAdapter.applyChanges(changeActions);
			LOG.info("Successfully replicate API-State into API-Manager");
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
