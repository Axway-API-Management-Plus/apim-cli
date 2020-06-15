package com.axway.apim.lib;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.RelaxedParser;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class APIMCoreCLIOptions {

	protected Options options = new Options();
	protected Options internalOptions = new Options();

	protected CommandLineParser parser = new RelaxedParser();
	
	protected String executable = "apim";

	/** This cmd contains all options visible in the usage when using help */
	protected CommandLine cmd;
	/** This CommandLine contains support, but hidden commands. Some of them are used to control testing */
	protected CommandLine internalCmd = null;

	String[] args;

	public APIMCoreCLIOptions(String[] args) {
		// Define core command line parameters!
		Option option = new  Option("h", "help", false, "Print the help");
		option.setRequired(false);
		options.addOption(option);
		
		option = new  Option("rc", "returncodes", false, "Print the possible return codes and description.");
		option.setRequired(false);
		options.addOption(option);
		
		// Parse at this point, if return codes should be shown
		showReturnCodes();

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
		
		option = new Option("force", "Optional flag used by different modules to enforce actions. For instance import breaking change or delete API(s).");
		options.addOption(option);
		
		// Added for backwards compatibility
		option = new Option("f", true, "Optional flag used by different modules to enforce actions. For instance import breaking change or delete API(s).");
		internalOptions.addOption(option);
		
		option = new Option("apimCLIHome", true, "The absolute path to the CLI home directory containing for instance your conf folder.\n"
				+ "You may also set the environment variable: '"+CommandParameters.APIM_CLI_HOME+"'");
		option.setRequired(false);
		option.setArgName("/home/chris/apim-cli");
		options.addOption(option);
		
		option = new Option("clearCache", true, "Clear the cache previously created, which will force the CLI to get fresh data from the API-Manager.\n"
				+ "Exmaples: 'ALL', '*application*', 'applicationsQuotaCache,*api*'");
		option.setRequired(false);
		option.setArgName("ALL");
		options.addOption(option);
		
		option = new Option("rollback", true, "Allows to disable the rollback feature");
		option.setRequired(false);
		option.setArgName("true");
		internalOptions.addOption(option);
		
		option = new Option("ignoreAdminAccount", "If set, the tool wont load the env.properties. This is used for testing only.");
		option.setRequired(false);
		internalOptions.addOption(option);
		
		option = new Option("returnCodeMapping", true, "Optionally maps given return codes into a desired return code. Format: 10:0, 12:0");
		option.setRequired(false);
		option.setArgName("true");
		internalOptions.addOption(option);
		
		option = new Option("ignoreCache", "The cache for REST-API calls isn't used.");
		option.setRequired(false);
		internalOptions.addOption(option);

		this.args = args;
	}

	/**
	 * Parse will use all declared options to create the cmd 
	 * AND additionally it uses internalOptions to create internalCmd. 
	 * Both is used to create the ultimately required CommandParameters which contains 
	 * a full set of options.
	 */
	void parse() {
		try {
			cmd = parser.parse(options, args);
			internalCmd = parser.parse( internalOptions, args);
		} catch (ParseException e) {
			printUsage(e.getMessage(), args);
			System.exit(99);
		}
		
		if(cmd.hasOption("help")) {
			printUsage("Usage information", args);
			System.exit(0);
		}

	}
	
	/**
	 * This is called automatically by the constructor to see the list of return-Codes.
	 */
	void showReturnCodes() {
		try {
			cmd = parser.parse(options, args);

			if(cmd.hasOption("returncodes")) {
				String spaces = "                                   ";
				System.out.println("Possible error codes and their meaning:\n");
				for(ErrorCode code : ErrorCode.values()) {
					System.out.println(code.name() + spaces.substring(code.name().length()) + "("+code.getCode()+")" + ": " + code.getDescription());
				}
				System.exit(0);
			}
		} catch (Exception e) {

		}
	}
	
	public void printUsage(String message, String[] args) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(new OptionsComparator());
		formatter.setWidth(140);
		
		formatter.printHelp(getAppName(), options, true);
		System.out.println("\n");
		System.out.println("ERROR: " + message);
		System.out.println("\n");
	}
	
	protected abstract String getAppName();
	
	/**
	 * @return name of the binary to call (.sh, .bat or .exe when using choco)
	 */
	protected String getBinaryName() {
		String binary;
		// Special handling when called from a Choco-Shiem executable
		if(args!=null && Arrays.asList(args).contains("choco")) {
			binary = this.executable;
		} else {
			String scriptExt = ".sh";
			if(System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = ".bat";
			binary = "scripts"+File.separator+this.executable+scriptExt;
		}
		return binary;
	}

	public CommandLine getCmd() {
		if(this.cmd==null) parse();
		return cmd;
	}

	public CommandLine getInternalCmd() {
		if(this.internalCmd==null) parse();
		return internalCmd;
	}

	public Options getOptions() {
		return options;
	}

	public Options getInternalOptions() {
		return internalOptions;
	}
	
	class OptionsComparator implements Comparator<Option> {
		
		private String[] basicOptions = {"host", "force", "username", "stage", "password", "returncodes", "port", "apimCLIHome"};

		@Override
		public int compare(Option option1, Option option2) {
			if(Arrays.asList(basicOptions).contains(option1.getLongOpt())) {
				return -1;
			}
			return 0;
		}
	}
}
