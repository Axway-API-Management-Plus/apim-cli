package com.axway.apim.lib;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public abstract class CLIOptions {

    public static final String VERSION = "version";
    private final CommandLineParser parser = new RelaxedParser();

    private String[] args;

    private final Options options = new Options();

    // to handle help and version
    private final Options optionalOptions = new Options();


    /**
     * This cmd contains all options visible in the usage when using help
     */
    private CommandLine cmd;
    private EnvironmentProperties envProperties;
    protected String executable = "apim";

    protected CLIOptions() {
        // Define core command line parameters!
        addHelpAndVersion();
    }

    protected CLIOptions(String[] args) {
        addHelpAndVersion();
        this.args = args;
    }

    public abstract Parameters getParams() throws AppException;

    public abstract void addOptions();

    public void addHelpAndVersion() {
        Option option = new Option("help", "Print the help");
        option.setRequired(false);
        optionalOptions.addOption(option);

        option = new Option(VERSION, "Print the APIM CLI Version number");
        option.setRequired(false);
        optionalOptions.addOption(option);
    }


    public String getValue(String key) {
        if (this.cmd != null && this.cmd.getOptionValue(key) != null) {
            return this.cmd.getOptionValue(key);
        } else if (this.envProperties != null && this.envProperties.containsKey(key)) {
            return this.envProperties.get(key);
        } else {
            return null;
        }
    }

    public boolean hasOption(String key) {
        return ((this.cmd != null && this.cmd.hasOption(key)) ||
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
            CommandLine commandLine = parser.parse(optionalOptions, args);
            if (commandLine.hasOption("help")) {
                printUsage("Usage information", args);
                throw new AppException("help", ErrorCode.SUCCESS);
            } else if (commandLine.hasOption(VERSION)) {
                Console.println(CLIOptions.class.getPackage().getImplementationVersion());
                throw new AppException(VERSION, ErrorCode.SUCCESS);
            }
            cmd = parser.parse(options, args);
            this.envProperties = new EnvironmentProperties(cmd.getOptionValue("stage"), getValue("apimCLIHome"));
        } catch (ParseException e) {
            printUsage(e.getMessage(), args);
            throw new AppException(e.getMessage(), ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public void printUsage(String message, String[] args) {
        Console.println("-----------------------------------------Command----------------------------------------");
        for (String arg:args) {
            Console.print(arg + " ");
        }
        Console.println("\n");
        Console.println("----------------------------------------------------------------------------------------");

        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new OptionsComparator());
        formatter.setWidth(140);

        formatter.printHelp(getAppName(), options, true);
        Console.println("\n");
        Console.println(message);
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
