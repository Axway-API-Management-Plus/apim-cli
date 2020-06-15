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
		
		option = new Option("deleteFolder", "Controls if an existing local folder should be deleted. Defaults to false.");
		options.addOption(option);
		
		option = new Option("f", "format", true, "Controls the export format. By default the console is used.");
		option.setRequired(false);
		option.setArgName("console|json");
		options.addOption(option);
	}
}
