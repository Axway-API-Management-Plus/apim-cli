package com.axway.apim.lib;

import org.apache.commons.cli.Option;

public class StandardImportCLIOptions  {

	public void addOptions(CLIOptions cliOptions) {
		Option option = new Option("enabledCaches", true, "By default, no cache is used for import actions. However, here you can enable caches if necessary to improve performance. Has no effect, when -ignoreCache is set. More information on the impact: https://bit.ly/3FjXRXE");
		option.setArgName("applicationsQuotaCache,*API*");
		cliOptions.addOption(option);

		option = new Option("stageConfig", true, "Manually provide the name of the stage configuration file to use instead of derived from the given stage.");
		option.setArgName("my-staged-config.json");
		cliOptions.addOption(option);
	}

}
