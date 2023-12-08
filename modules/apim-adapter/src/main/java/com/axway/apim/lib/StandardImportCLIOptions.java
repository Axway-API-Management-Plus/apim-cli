package com.axway.apim.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.error.AppException;

public class StandardImportCLIOptions extends CLIOptions {

	private final CLIOptions cliOptions;

	public StandardImportCLIOptions(CLIOptions cliOptions) {
		this.cliOptions = cliOptions;
	}

	@Override
	public Parameters getParams() throws AppException {
		StandardImportParams params = (StandardImportParams)cliOptions.getParams();

		params.setEnabledCaches(getValue("enabledCaches"));
		params.setStageConfig(getValue("stageConfig"));
		return params;
	}

	@Override
	public void addOptions() {
		cliOptions.addOptions();

		Option option = new Option("enabledCaches", true, "By default, no cache is used for import actions. However, here you can enable caches if necessary to improve performance. Has no effect, when -gnoreCache is set. More information on the impact: https://bit.ly/3FjXRXE");
		option.setArgName("applicationsQuotaCache,*API*");
		cliOptions.addOption(option);

		option = new Option("stageConfig", true, "Manually provide the name of the stage configuration file to use instead of derived from the given stage.");
		option.setArgName("my-staged-config.json");
		cliOptions.addOption(option);
	}

	@Override
	public void addOption(Option option) {
		cliOptions.addOption(option);
	}


	@Override
	public void parse() throws AppException{
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
