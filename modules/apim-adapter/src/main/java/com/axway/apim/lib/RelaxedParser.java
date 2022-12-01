package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RelaxedParser extends DefaultParser {

	@Override
    public CommandLine parse(Options options, String[] arguments) throws ParseException {
        List<String> knownArguments = new ArrayList<>();
        for (int i=0;i<arguments.length;) {
        	String opt = arguments[i];
        	i++;
        	// Verify the option is known to this set of options
        	if (options.hasOption(opt)) {
        		// If know, transfer it to the known options
        		knownArguments.add(opt);
        		// Check, if the option ...
        		Option option = options.getOption(opt);
        		// should have an argument 
        		if(!option.hasArg()) {
        			// If not continue to the next argument
				} else {
        			// If yes, grab the argument value and transfer it
        			String value = arguments[i];
        			knownArguments.add(value);
        			i++;
        		}
        	}
        }
        return super.parse(options, knownArguments.toArray(new String[0]));
    }
}
