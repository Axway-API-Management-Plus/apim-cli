package com.axway.apim.metadata.export;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.lib.RelaxedParser;
import com.axway.apim.metadata.export.formats.CSVCustomPolicyDependencyReport;
import com.axway.apim.metadata.export.formats.CSVEmbeddedAnalyticsReport;
import com.axway.apim.metadata.export.formats.ExcelCustomPolicyDependencyReport;
import com.axway.apim.swagger.APIManagerAdapter;

public class APIManagerMetadataExport {

	private static Logger LOG = LoggerFactory.getLogger(APIManagerMetadataExport.class);

	public static void main(String args[]) { 
		int rc = run(args);
		System.exit(rc);
	}
	
	public static int run(String args[]) {
		CommandParameters params;
		try {
			Options options = new Options();
			Option option;
			
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
			
			option = new Option("f", "filename", true, "Filename used to export meta-data configuration (relative or absolute).");
			option.setRequired(true);
			option.setArgName("api-mgr-prod-metadata-export.csv");
			options.addOption(option);
			
			option = new Option("r", "report", true, "Type of report you want.");
			option.setRequired(true);
			option.setArgName(CSVEmbeddedAnalyticsReport.class.getSimpleName());
			options.addOption(option);
			
			CommandLineParser parser = new RelaxedParser();
			
			CommandLine cmd = null;
			
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				printUsage(options, e.getMessage());
				System.exit(99);
			}
			LOG.info("------------------------------------------------------------------------");
			LOG.info("API-Manager Metadata-Export Version: 1.0.0");
			LOG.info("                                                                        ");
			LOG.info("To report issues or get help, please visit: ");
			LOG.info("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote");
			LOG.info("------------------------------------------------------------------------");
			
			params = new CommandParameters(cmd);
			
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			APIExportMetadataHandler exportHandler = new APIExportMetadataHandler(apimAdapter, params.getValue("report"));
			exportHandler.exportMetadata();
		} catch (AppException ap) {
			APIPropertiesExport.getInstance().store(); // Try to create it, even 
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
		LOG.info("Successfully exported API-Metadata information.");
		return 0;
	}
	
	private static void printUsage(Options options, String message) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(140);
		String scriptExt = "sh";
		if(System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = "bat";
		
		formatter.printHelp("API-Manager Meta-Data Export", options, true);
		System.out.println("\n");
		System.out.println("Tool to query your API-Manager registry and generate reports in some formats.");
		System.out.println("Originally invented to feed API-Metadata into Axway EA4APIM. Flexible for other formats");
		System.out.println("\n");
		System.out.println("The following Report-Formats are supported:");
		System.out.println(CSVEmbeddedAnalyticsReport.class.getSimpleName()+":\nGenerates a report with APIs, Applications, Orgs and their relation to each other. (CSV-Format)");
		System.out.println(CSVCustomPolicyDependencyReport.class.getSimpleName()+":\nTells you which APIs are using which Custom-Policies (CSV-Format)");
		System.out.println(ExcelCustomPolicyDependencyReport.class.getSimpleName()+":\nTells you which APIs are using which Custom-Policies (Excel-Format)");
		System.out.println("\n");
		System.out.println("ERROR: " + message);
		System.out.println("\n");
		System.out.println("You may run one of the following examples:");
		System.out.println("scripts"+File.separator+"run-metadata-export."+scriptExt+" -h localhost -u apiadmin -p changeme -f custom-policies -r " +ExcelCustomPolicyDependencyReport.class.getSimpleName());
		System.out.println();
		System.out.println("For more information visit: https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki");
	}
}
