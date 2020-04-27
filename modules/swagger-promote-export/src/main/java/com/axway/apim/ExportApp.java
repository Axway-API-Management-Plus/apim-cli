package com.axway.apim;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.export.APIExportConfigAdapter;
import com.axway.apim.api.export.lib.ExportCommandParameters;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.RelaxedParser;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.lib.utils.rest.Transaction;

/**
 * 
 * @author cwiechmann@axway.com
 */
public class ExportApp {

	private static Logger LOG = LoggerFactory.getLogger(ExportApp.class);

	public static void main(String args[]) { 
		int rc = run(args);
		System.exit(rc);
	}
		
	public static int run(String args[]) {
		try {
			Options options = new Options();
			Option option;
			
			option = new Option("a", "api-path", true, "Define the APIs to be exported, based on the exposure path.\n"
					+ "You can use wildcards to export multiple APIs:\n"
					+ "-a /api/v1/my/great/api     : Export a specific API\n"
					+ "-a *                        : Export all APIs\n"
					+ "-a /api/v1/any*             : Export all APIs with this prefix\n"
					+ "-a */some/other/api         : Export APIs end with the same path\n");
				option.setRequired(true);
				option.setArgName("/api/v1/my/great/api");
			options.addOption(option);
			
			option = new Option("v", "vhost", true, "Limit the export to that specific host.");
			option.setRequired(false);
			option.setArgName("vhost.customer.com");
			options.addOption(option);
			
			option = new Option("l", "localFolder", true, "Defines the location to store API-Definitions locally. Defaults to current folder.\n"
					+ "For each API a new folder is created automatically.");
				option.setRequired(false);
				option.setArgName("my/apis");
			options.addOption(option);			
			
			option = new Option("h", "host", true, "The API-Manager hostname from where to export");
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
			
			option = new Option("s", "stage", true, "The Stage or basically the API-Manager environment you want use.\n"
					+ "Provide an env.<stage>.properties with required parameters like host, username, password.");
				option.setArgName("preprod");
			options.addOption(option);
			
			option = new Option("df", "deleteFolder", true, "Controls if an existing local folder should be deleted. Defaults to false.");
			option.setArgName("true");
			options.addOption(option);
			
			Options internalOptions = new Options();
			option = new  Option("h", "help", false, "Print the help");
			option.setRequired(false);
			internalOptions.addOption(option);
			
			option = new Option("ignoreAdminAccount", true, "If set, the tool wont load the env.properties. This is used for testing only.");
			option.setRequired(false);
			option.setArgName("true");
			internalOptions.addOption(option);
			
			CommandLineParser parser = new RelaxedParser();
			CommandLine cmd = null;
			CommandLine internalCmd = null;
			
			System.out.println("------------------------------------------------------------------------");
			System.out.println("API-Manager Promote: "+App.class.getPackage().getImplementationVersion() + " - E X P O R T");
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
			
			ExportCommandParameters params = new ExportCommandParameters(cmd, internalCmd, new EnvironmentProperties(cmd.getOptionValue("stage")));
			
			APIExportConfigAdapter exportAdapter = new APIExportConfigAdapter(params.getValue("api-path"), params.getValue("localFolder"), params.getValue("vhost"));
			exportAdapter.exportAPIs();
			return 0;
		} catch (AppException ap) {
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				return errorState.getErrorCode().getCode();
			} else {
				LOG.error(ap.getMessage(), ap);
				return ap.getErrorCode().getCode();
			}
		} catch (Exception e) {

			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	private static void printUsage(Options options, String message, String[] args) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(140);
		// Special handling when called from a Choco-Shiem executable
		String binary;
		if(args!=null && Arrays.asList(args).contains("choco")) {
			binary = "api-export";
		} else {
			String scriptExt = "sh";
			if(System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = "bat";
			binary = "scripts"+File.separator+"api-export."+scriptExt;
		}
		
		formatter.printHelp("API-Export", options, true);
		System.out.println("\n");
		System.out.println("ERROR: " + message);
		System.out.println("\n");
		System.out.println("You may run one of the following examples:");
		System.out.println(binary+" -a /api/v1/ -l my_apis -h location -u apiadmin -p changeme");
		System.out.println(binary+" -a \"/api/v1/*\" -l my_apis -h location -u apiadmin -p changeme");
		System.out.println();
		System.out.println("Using parameters provided in properties file stored in conf-folder:");
		System.out.println(binary+" -a /api/v1/ -l my_apis -s api-env");
		System.out.println(binary+" -a \"*\" -l all_my_apis -s api-env");
		System.out.println();
		System.out.println("For more information visit: https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki");
	}
}
