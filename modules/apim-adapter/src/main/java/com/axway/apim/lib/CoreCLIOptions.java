package com.axway.apim.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.errorHandling.AppException;

public class CoreCLIOptions extends CLIOptions {
	
	private CLIOptions cliOptions;

	public CoreCLIOptions(CLIOptions cliOptions) {
		this.cliOptions = cliOptions;
	}

	@Override
	public Parameters getParams() throws AppException {
		CoreParameters params = (CoreParameters)cliOptions.getParams();
		
		params.setProperties(getEnvProperties());
		
		params.setApimCLIHome(getValue("apimCLIHome"));
		params.setStage(getValue("stage"));
		params.setHostname(getValue("host"));
		params.setApiBasepath(getValue("apiBasepath"));
		params.setPort((getValue("port")!=null) ? Integer.parseInt(getValue("port").trim()) : -1);
		params.setUsername(getValue("username"));
		params.setPassword(getValue("password"));
		params.setAdminUsername(getValue("admin_username"));
		params.setAdminPassword(getValue("admin_password"));
		params.setClearCache(getValue("clearCache"));
		
		params.setReturnCodeMapping(getValue("returnCodeMapping"));
		//
		params.setForce(hasOption("force"));
		params.setIgnoreAdminAccount(hasOption("ignoreAdminAccount"));
		params.setIgnoreCache(hasOption("ignoreCache"));
		if(getValue("rollback")!=null) params.setRollback(Boolean.parseBoolean(getValue("rollback")));
		
		// Also support -f for backwards compatibility
		if(!params.isForce()) params.setForce(Boolean.parseBoolean(getValue("f")));
		
		params.setProxyHost(getValue("httpProxyHost"));
		params.setProxyPort((getValue("httpProxyPort")!=null) ? Integer.valueOf(getValue("httpProxyPort")) : null);
		params.setProxyUsername(getValue("httpProxyUsername"));
		params.setProxyPassword(getValue("httpProxyPassword"));
		params.setRetryDelay(getValue("retryDelay"));
		
		
		return (Parameters) params;
	}

	@Override
	public void addOptions() {
		cliOptions.addOptions();
		Option option = new Option("s", "stage", true, "The API-Management stage (prod, preprod, qa, etc.)\n"
				+ "Is used to lookup the stage configuration file.");
		option.setArgName("preprod");
		cliOptions.addOption(option);
		
		// Define core command line parameters!
		option = new  Option("h", "help", false, "Print the help");
		option.setRequired(false);
		cliOptions.addOption(option);
		
		option = new  Option("returncodes", false, "Print the possible return codes and description.");
		option.setRequired(false);
		cliOptions.addOption(option);
		
		// Parse at this point, if return codes should be shown
		cliOptions.showReturnCodes();

		option = new Option("h", "host", true, "The API-Manager hostname the API should be imported");
		option.setRequired(false);
		option.setArgName("api-host");
		cliOptions.addOption(option);

		option = new Option("port", true, "Optional parameter to declare the API-Manager port. Defaults to 8075.");
		option.setArgName("8181");
		cliOptions.addOption(option);

		option = new Option("u", "username", true, "Username used to authenticate. Please note, that this user must have Admin-Role");
		option.setRequired(false);
		option.setArgName("apiadmin");
		cliOptions.addOption(option);

		option = new Option("p", "password", true, "Password used to authenticate");
		option.setRequired(false);
		option.setArgName("changeme");
		cliOptions.addOption(option);
		
		option = new Option("force", "Optional flag used by different modules to enforce actions. For instance import breaking change or delete API(s).");
		cliOptions.addOption(option);
		
		// Added for backwards compatibility
		option = new Option("f", true, "Optional flag used by different modules to enforce actions. For instance import breaking change or delete API(s).");
		cliOptions.addInternalOption(option);
		
		option = new Option("apimCLIHome", true, "The absolute path to the CLI home directory containing for instance your conf folder.\n"
				+ "You may also set the environment variable: '"+CoreParameters.APIM_CLI_HOME+"'");
		option.setRequired(false);
		option.setArgName("/home/chris/apim-cli");
		cliOptions.addOption(option);
		
		option = new Option("clearCache", true, "Clear the cache previously created, which will force the CLI to get fresh data from the API-Manager.\n"
				+ "Examples: 'ALL', '*application*', 'applicationsQuotaCache,*api*'");
		option.setRequired(false);
		option.setArgName("ALL");
		cliOptions.addOption(option);
		
		option = new Option("ignoreCache", "The cache for REST-API calls against the API-Manager isn't used at all.");
		option.setRequired(false);
		cliOptions.addOption(option);
		
		option = new Option("rollback", true, "Allows to disable the rollback feature");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("ignoreAdminAccount", "If set, the tool wont load the env.properties. This is used for testing only.");
		option.setRequired(false);
		cliOptions.addInternalOption(option);
		
		option = new Option("returnCodeMapping", true, "Optionally maps given return codes into a desired return code. Format: 10:0, 12:0");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("httpProxyHost", true, "Name of the proxy host");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("httpProxyPort", true, "The proxy port");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("httpProxyUsername", true, "The proxy username");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("httpProxyPassword", true, "The proxy username");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("apiBasepath", true, "The API-Manager REST-API base path. Defaults to: " + CoreParameters.DEFAULT_API_BASEPATH);
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
		
		option = new Option("retryDelay", true, "Retry delay in milliseconds for the some of the flaky REST-API-Manager API-Calls. See issue #213");
		option.setRequired(false);
		option.setArgName("true");
		cliOptions.addInternalOption(option);
	}

	@Override
	public void addOption(Option option) {
		cliOptions.addOption(option);
	}

	@Override
	public void addInternalOption(Option option) {
		cliOptions.addInternalOption(option);
	}

	@Override
	public void parse() {
		cliOptions.parse();
		
	}

	@Override
	public String getValue(String key) {
		return cliOptions.getValue(key);
	}

	@Override
	public void printUsage(String message, String[] args) {
		cliOptions.printUsage(message, args);
	}

	@Override
	public void showReturnCodes() {
		cliOptions.showReturnCodes();
	}

	@Override
	public boolean hasOption(String key) {
		return cliOptions.hasOption(key);
	}

	@Override
	public EnvironmentProperties getEnvProperties() {
		return cliOptions.getEnvProperties();
	}
}
