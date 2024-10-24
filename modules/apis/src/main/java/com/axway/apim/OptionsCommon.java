package com.axway.apim;

import com.axway.apim.lib.CLIOptions;
import org.apache.commons.cli.Option;

public class OptionsCommon {

    public void addDeprecateAndRetired(CLIOptions options){
        Option  option = new Option("refAPIDeprecate", true, "If set the old/reference API will be flagged as deprecated. Defaults to false.");
        option.setRequired(false);
        option.setArgName("true");
        options.addOption(option);

        option = new Option("refAPIRetire", true, "If set the old/reference API will be retired. Default to false.");
        option.setRequired(false);
        option.setArgName("true");
        options.addOption(option);

        option = new Option("refAPIRetireDate", true, "Sets the retirement date of the old API. Supported formats: \"dd.MM.yyyy\", \"dd/MM/yyyy\", \"yyyy-MM-dd\", \"dd-MM-yyyy\"");
        option.setRequired(false);
        option.setArgName("2021/06/30");
        options.addOption(option);
    }
}
