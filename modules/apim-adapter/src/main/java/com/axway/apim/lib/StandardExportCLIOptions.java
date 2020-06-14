package com.axway.apim.lib;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public abstract class StandardExportCLIOptions extends APIMCoreCLIOptions {

	public StandardExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("w", "wide", true, "Used to control the amount of data shown in list views. Possible values: standard, wide, ultra. Requesting more data has a performance impact.");
		option.setRequired(false);
		option.setArgName("ultra");
		options.addOption(option);
	}
}
