package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RelaxedParser extends DefaultParser {

	@Override
    public CommandLine parse(Options options, String[] arguments) throws ParseException {
        List<String> knownArguments = new ArrayList<>();
        boolean addOptionValue = false;
        for (String arg : arguments) {
            if (options.hasOption(arg) || addOptionValue) {
                knownArguments.add(arg);
                addOptionValue = (addOptionValue) ? false : true;
            }
        }
        return super.parse(options, knownArguments.toArray(new String[knownArguments.size()]));
    }
}
