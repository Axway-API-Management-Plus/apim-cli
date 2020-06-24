package com.axway.apim.lib;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public abstract class StandardExportCLIOptions extends APIMCoreCLIOptions {

	public StandardExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("wide", "A wider view of data to be returned by the export implementation. Requesting more data has a performance impact.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("ultra", "Get most of the data to be returned by the export implementation. Requesting more data has a performance impact.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("deleteTarget", "Controls if an existing target folder or file should be deleted. Defaults to false.");
		options.addOption(option);
		
		option = new Option("t", "target", true, "Defines the target location for get operations that are creating files or directories.\n"
				+ "Defaults to current folder. Required output files or directories are created automatically.");
		option.setRequired(false);
		option.setArgName("my/apis");
		options.addOption(option);
		
		option = new Option("o", "output", true, "Controls the output format. By default the console is used.");
		option.setRequired(false);
		option.setArgName("console|json|csv");
		options.addOption(option);
	}
}
