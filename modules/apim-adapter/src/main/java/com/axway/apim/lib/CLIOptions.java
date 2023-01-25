package com.axway.apim.lib;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class CLIOptions {

    private final CommandLineParser parser = new RelaxedParser();

    private String[] args;

    private final Options options = new Options();
    private final Options internalOptions = new Options();

    /**
     * This cmd contains all options visible in the usage when using help
     */
    private CommandLine cmd;
    /**
     * This CommandLine contains support, but hidden commands. Some of them are used to control testing
     */
    private CommandLine internalCmd = null;

    private EnvironmentProperties envProperties;

    protected String executable = "apim";

    protected CLIOptions() {
    }

    public CLIOptions(String[] args) {
        super();
        this.args = args;
    }

    public abstract Parameters getParams() throws AppException;

    public abstract void addOptions();

    public String getValue(String key) {
        if (this.cmd != null && this.cmd.getOptionValue(key) != null) {
            return this.cmd.getOptionValue(key);
        } else if (this.internalCmd != null && this.internalCmd.getOptionValue(key) != null) {
            return this.internalCmd.getOptionValue(key);
        } else if (this.envProperties != null && this.envProperties.containsKey(key)) {
            return this.envProperties.get(key);
        } else {
            return null;
        }
    }

    public boolean hasOption(String key) {
        return ((this.cmd != null && this.cmd.hasOption(key)) ||
                (this.cmd != null && this.internalCmd.hasOption(key)) ||
                (this.envProperties != null && this.envProperties.containsKey(key)));
    }

    /**
     * Parse will use all declared options to create the cmd
     * AND additionally it uses internalOptions to create internalCmd.
     * Both is used to create the ultimately required CommandParameters which contains
     * a full set of options.
     */
    public void parse() throws AppException {
        try {
            cmd = parser.parse(options, args);
            internalCmd = parser.parse(internalOptions, args);
            this.envProperties = new EnvironmentProperties(cmd.getOptionValue("stage"), getValue("apimCLIHome"));
        } catch (Exception e) {
            printUsage(e.getMessage(), args);
            throw new AppException(e.getMessage(), ErrorCode.UNXPECTED_ERROR, e);
        }
        if (cmd.hasOption("help")) {
            printUsage("Usage information", args);
            System.exit(0);
        }
    }

    public void printUsage(String message, String[] args) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new OptionsComparator());
        formatter.setWidth(140);

        formatter.printHelp(getAppName(), options, true);
        Console.println("\n");
        Console.println("ERROR: " + message);
        Console.println("\n");
    }

    protected String getAppName() {
        return "APIM-CLI";
    }

    static class OptionsComparator implements Comparator<Option> {

        private final String[] basicOptions = {"host", "force", "username", "stage", "password", "returncodes", "port", "apimCLIHome", "ignoreCache", "clearCache"};

        @Override
        public int compare(Option option1, Option option2) {
            if (Arrays.asList(basicOptions).contains(option1.getLongOpt()) || Arrays.asList(basicOptions).contains(option1.getOpt())) {
                return -1;
            }
            return 0;
        }
    }

    public void addOption(Option option) {
        this.options.addOption(option);
    }

    public void addInternalOption(Option option) {
        this.internalOptions.addOption(option);
    }

    /**
     * @return name of the binary to call (.sh, .bat or .exe when using choco)
     */
    protected String getBinaryName() {
        String binary;
        // Special handling when called from a Choco-Shiem executable
        if (args != null && Arrays.asList(args).contains("choco")) {
            binary = this.executable;
        } else {
            String scriptExt = ".sh";
            if (System.getProperty("os.name").toLowerCase().contains("win")) scriptExt = ".bat";
            binary = "scripts" + File.separator + this.executable + scriptExt;
        }
        return binary;
    }

    /**
     * This is called automatically by the constructor to see the list of return-Codes.
     */
    public void showReturnCodes() {
        for (String arg : args) {
            if ("-returncodes".equals(arg)) {
                String spaces = "                                   ";
                Console.println("Possible error codes and their meaning:\n");
                for (ErrorCode code : ErrorCode.values()) {
                    Console.println(code.name() + spaces.substring(code.name().length()) + "(" + code.getCode() + ")" + ": " + code.getDescription());
                }
                System.exit(0);
            }
        }
    }

    public EnvironmentProperties getEnvProperties() {
        return envProperties;
    }
}
