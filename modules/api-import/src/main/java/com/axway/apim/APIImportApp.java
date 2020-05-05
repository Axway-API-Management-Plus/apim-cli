package com.axway.apim;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIMgrProxiesAdapter;
import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.apiimport.ActualAPI;
import com.axway.apim.apiimport.rollback.RollbackHandler;
import com.axway.apim.apiimport.state.APIChangeState;
import com.axway.apim.lib.APIMCLIServiceProvider;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.RelaxedParser;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.lib.utils.rest.Transaction;

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
public class APIImportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIImportApp.class);

	public static void main(String args[]) { 
		int rc = run(args);
		System.exit(rc);
	}
		
	public static int run(String args[]) {
		ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
		try {
			CommandLineParser parser = new RelaxedParser();
			CommandLine cmd = null;
			CommandLine internalCmd = null;
			
			Options options = new Options();
			Option option;
			
			option = new  Option("h", "help", false, "Print the help");
			option.setRequired(false);
			options.addOption(option);
			
			option = new  Option("rc", "returncodes", false, "Print the possible return codes and description.");
			option.setRequired(false);
			options.addOption(option);
			
			cmd = parser.parse(options, args);

			if(cmd.hasOption("returncodes")) {
				String spaces = "                                   ";
				System.out.println("Possible error codes and their meaning:\n");
				for(ErrorCode code : ErrorCode.values()) {
					System.out.println(code.name() + spaces.substring(code.name().length()) + "("+code.getCode()+")" + ": " + code.getDescription());
				}
				System.exit(0);
			}
			
			option = new Option("a", "apidefinition", true, "(Optional) The API Definition either as Swagger (JSON-Formated) or a WSDL for SOAP-Services:\n"
					+ "- in local filesystem using a relative or absolute path. Example: swagger_file.json\n"
					+ "  Please note: Local filesystem is not supported for WSDLs. Please use direct URL or a URL-Reference-File.\n"
					+ "- a URL providing the Swagger-File or WSDL-File. Examples:\n"
					+ "  [username/password@]https://any.host.com/my/path/to/swagger.json\n"
					+ "  [username/password@]http://www.dneonline.com/calculator.asmx?wsdl\n"
					+ "- a reference file called anyname-i-want.url which contains a line with the URL\n"
					+ "  (same format as above for Swagger or WSDL)."
					+ "  If not specified, the API Definition configuration is read directly from the JSON-Formatted API-Config");
				option.setRequired(false);
				option.setArgName("swagger_file.json");
			options.addOption(option);
			
			option = new Option("c", "contract", true, "This is the JSON-Formatted API-Config containing information how to expose the API");
				option.setRequired(true);
				option.setArgName("api_config.json");
			options.addOption(option);
			
			option = new Option("s", "stage", true, "The stage this API should be imported.\n"
					+ "Will be used to lookup stage specific API-Config overrides (e.g.: api_config.preprod.json)");
				option.setArgName("preprod");
			options.addOption(option);
			
			option = new Option("h", "host", true, "The API-Manager hostname the API should be imported");
				option.setRequired(false);
				option.setArgName("api-host");
			options.addOption(option);
			
			option = new Option("port", true, "Optional parameter to declare the API-Manager port. Defaults to 8075.");
			option.setArgName("8181");
			options.addOption(option);
			
			option = new Option("u", "username", true, "Username used to authenticate. Please note, that this user must have Admin-Role");
				option.setRequired(false);
				option.setArgName("apiadmin");
			options.addOption(option);
			
			option = new Option("p", "password", true, "Password used to authenticate");
				option.setRequired(false);
				option.setArgName("changeme");
			options.addOption(option);
			
			option = new Option("f", "force", true, "Breaking changes can't be imported without this flag, unless the API is unpublished.");
			option.setArgName("true/[false]");
			options.addOption(option);
			
			option = new Option("swaggerPromoteHome", true, "The absolute path to the Swagger-Promote home directory containing for instance your conf folder.\n"
					+ "You may also set the environment variable: '"+CommandParameters.SWAGGER_PROMOTE_HOME+"'");
			option.setRequired(false);
			option.setArgName("/home/chris/swagger-promote");
			options.addOption(option);
			
			option = new Option("iq", "ignoreQuotas", true, "Use this flag to ignore configured API quotas.");
			option.setArgName("true/[false]");
			options.addOption(option);
			
			option = new Option("clientOrgsMode", true, "Controls how configured Client-Organizations are treated. Defaults to add!");
			option.setArgName("ignore|replace|add");
			options.addOption(option);
			
			option = new Option("clientAppsMode", true, "Controls how configured Client-Applications are treated. Defaults to add!");
			option.setArgName("ignore|replace|add");
			options.addOption(option);
			
			option = new Option("quotaMode", true, "Controls how quotas are managed in API-Manager. Defaults to add!");
			option.setArgName("ignore|replace|add");
			options.addOption(option);
			
			option = new Option("detailsExportFile", true, "Configure a filename, to get a Key=Value file containing information about the created API.");
			option.setRequired(false);
			option.setArgName("APIDetails.properties");
			options.addOption(option);
			
			Options internalOptions = new Options();
			option = new Option("ignoreAdminAccount", true, "If set, the tool wont load the env.properties. This is used for testing only.");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			option = new Option("allowOrgAdminsToPublish", true, "If set to false, OrgAdmins cannot replicate an API with desired state published. Defaults to true.");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			option = new Option("replaceHostInSwagger", true, "Controls if you want to replace the host in your Swagger-File ");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			option = new Option("rollback", true, "Allows to disable the rollback feature");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			option = new Option("returnCodeMapping", true, "Optionally maps given return codes into a desired return code. Format: 10:0, 12:0");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			option = new Option("changeOrganization", true, "Set this flag to true to allow to change the organization of an existing API. Default is false.");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			System.out.println("------------------------------------------------------------------------");
			System.out.println("API-Manager Promote: "+APIImportApp.class.getPackage().getImplementationVersion() + " - I M P O R T");
			System.out.println("                                                                        ");
			System.out.println("To report issues or get help, please visit: ");
			System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote");
			System.out.println("------------------------------------------------------------------------");
			System.out.println("");
			
			try {
				cmd = parser.parse(options, args);
				internalCmd = parser.parse( internalOptions, args);
			} catch (ParseException e) {
				printUsage(options, e.getMessage(), args);
				System.exit(99);
			}
			
			if(cmd.hasOption("help")) {
				printUsage(options, "Usage information", args);
				System.exit(0);
			}

			
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();
			Transaction.deleteInstance();
			RollbackHandler.deleteInstance();
			
			CommandParameters params = new CommandParameters(cmd, internalCmd, new EnvironmentProperties(cmd.getOptionValue("stage"), cmd.getOptionValue("swaggerPromoteHome")));
			errorCodeMapper.setMapConfiguration(params.getValue("returnCodeMapping"));
			
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			
			APIImportConfigAdapter configAdapter = new APIImportConfigAdapter(params.getValue("contract"), 
					params.getValue("stage"), params.getValue("apidefinition"), apimAdapter.isUsingOrgAdmin());
			// Creates an API-Representation of the desired API
			IAPI desiredAPI = configAdapter.getDesiredAPI();
			// 
			List<NameValuePair> filters = new ArrayList<NameValuePair>();
			// If we don't have an AdminAccount available, we ignore published APIs - For OrgAdmins 
			// the unpublished or pending APIs become the actual API
			if(!APIManagerAdapter.hasAdminAccount()) {
				filters.add(new BasicNameValuePair("field", "state"));
				filters.add(new BasicNameValuePair("op", "ne"));
				filters.add(new BasicNameValuePair("value", "published"));
			}
			// Lookup an existing APIs - If found the actualAPI is valid - desiredAPI is used to control what needs to be loaded
			IAPI actualAPI = apimAdapter.getAPIManagerAPI(new APIMgrProxiesAdapter.Builder(APIManagerAdapter.TYPE_FRONT_END)
					.hasApiPath(desiredAPI.getPath())
					.hasVHost(desiredAPI.getVhost())
					.hasQueryStringVersion(desiredAPI.getApiRoutingKey())
					.useFilter(filters)
					.build().getAPI(true), desiredAPI, ActualAPI.class);
			// Based on the actual API - fulfill/complete some elements in the desired API
			configAdapter.completeDesiredAPI(desiredAPI, actualAPI);
			APIChangeState changeActions = new APIChangeState(actualAPI, desiredAPI);
			new APIImportManager().applyChanges(changeActions);
			APIPropertiesExport.getInstance().store();
			LOG.info("Successfully replicated API-State into API-Manager");
			return 0;
		} catch (AppException ap) {
			APIPropertiesExport.getInstance().store(); // Try to create it, even 
			ErrorState errorState = ErrorState.getInstance();
			if(!ap.getErrorCode().equals(ErrorCode.NO_CHANGE)) {
				RollbackHandler rollback = RollbackHandler.getInstance();
				rollback.executeRollback();
			}
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode();
			} else {
				LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(ap.getErrorCode()).getCode();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	private static void printUsage(Options options, String message, String[] args) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(140);
		String binary;
		// Special handling when called from a Choco-Shiem executable
		if(args!=null && Arrays.asList(args).contains("choco")) {
			binary = "api-import";
		} else {
			String scriptExt = "sh";
			if(System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = "bat";
			binary = "scripts"+File.separator+"api-import."+scriptExt;
		}
		
		formatter.printHelp("API-Import", options, true);
		System.out.println("\n");
		System.out.println("ERROR: " + message);
		System.out.println("\n");
		System.out.println("You may run one of the following examples:");
		System.out.println(binary+" -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println(binary+" -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme -s prod");
		System.out.println(binary+" -c samples/complex/complete-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println();
		System.out.println("Using parameters provided in properties file stored in conf-folder:");
		System.out.println(binary+" -c samples/basic/minimal-config-api-definition.json -s api-env");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/tree/develop/modules/swagger-promote-core/src/main/assembly/samples");
		System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki");
	}

	@Override
	public String getId() {
		return "api";
	}
	
	@Override
	public String getVersion() {
		return APIImportApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getMethod() {
		return "import";
	}

	@Override
	public String getDescription() {
		return "Import APIs into the API-Manager";
	}
	
	public String getName() {
		return "API Import";
	}

	@Override
	public int execute(String[] args) {
		return run(args);
	}
}
