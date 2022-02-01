package com.axway.apim.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;

public class StandardExportCLIOptions extends CLIOptions {
	
	private CLIOptions cliOptions;

	public StandardExportCLIOptions(CLIOptions cliOptions) {
		this.cliOptions = cliOptions;
	}
	
	@Override
	public Parameters getParams() throws AppException {
		StandardExportParams params = (StandardExportParams)cliOptions.getParams();
		
		if(hasOption("wide")) params.setWide(Wide.wide);
		if(hasOption("ultra")) params.setWide(Wide.ultra);
		params.setDeleteTarget(hasOption("deleteTarget"));
		params.setTarget(getValue("target"));
		params.setOutputFormat(OutputFormat.getFormat(getValue("o")));
		
		return (Parameters) params;
	}
	
	@Override
	public void addOptions() {
		cliOptions.addOptions();
		
		Option option = new Option("wide", "A wider view of data to be returned by the export implementation. Requesting more data has a performance impact.");
		option.setRequired(false);
		cliOptions.addOption(option);
		
		option = new Option("ultra", "Get most of the data to be returned by the export implementation. Requesting more data has a performance impact.");
		option.setRequired(false);
		cliOptions.addOption(option);
		
		option = new Option("deleteTarget", "Controls if an existing target folder or file should be deleted. Defaults to false.");
		cliOptions.addOption(option);
		
		option = new Option("t", "target", true, "Defines the target location for get operations that are creating files or directories.\n"
				+ "Defaults to current folder. Required output files or directories are created automatically.");
		option.setRequired(false);
		option.setArgName("my/apis");
		cliOptions.addOption(option);
		
		option = new Option("o", "output", true, "Controls the output format. By default the console is used. CSV and DAT is not supported for all entities.");
		option.setRequired(false);
		option.setArgName("console|json|csv|dat");
		cliOptions.addOption(option);
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
