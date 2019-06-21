package com.axway.apim.promote.mvn.plugin;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.axway.apim.actions.rest.APIMHttpClient;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.lib.RelaxedParser;
import com.axway.apim.promote.mvn.plugin.exceptions.AbstractPublishingPluginException;
import com.axway.apim.promote.mvn.plugin.exceptions.ArgumentParseException;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIImportConfigAdapter;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.IAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A adapter to handle with axway-swagger-promote tool
 */
public class AxwayPublishingAdapter {

    private final Logger logger = Logger.getLogger(AxwayPublishingAdapter.class.getName());

    // the singleton instance of this class
    private static AxwayPublishingAdapter axwayPublishingAdapter;

    /**
     * Singleton for this class. When creating the instance, axway internal singletons will be cleaned
     * @return synchronized instance of this class
     */
    public static synchronized AxwayPublishingAdapter instance() {
        if (axwayPublishingAdapter == null) {
            axwayPublishingAdapter = new AxwayPublishingAdapter();
        }

        return axwayPublishingAdapter;
    }

    /**
     * Clean this instance.
     * This is needed for the unit tests. Otherwise the tests will be working with the wrong internal axway error state
     */
    private AxwayPublishingAdapter() {
        cleanAxwayInstance();
    }

    /**
     * Clean all axway internal singletons
     */
    private void cleanAxwayInstance() {
        try {
            APIManagerAdapter.deleteInstance();
            ErrorState.deleteInstance();
            APIMHttpClient.deleteInstance();
            Transaction.deleteInstance();
        } catch (AppException e) {
            logger.error(e);
        }
    }


    /**
     * Clean axway instance. This is a copy of the implementation of the axway-swagger-promote tool.
     * In a real maven plugin this step should not be needed anymore
     */
    public static synchronized void cleanInstance() {
        axwayPublishingAdapter = null;
    }

    /**
     * Initialize commandline options and args.
     * This step is needed because the way axway-swagger-promote tool is wired.
     *
     * @throws ParseException
     * @throws AppException
     */
    public void initOptionsAndArgs(final Publication publication)
            throws AbstractPublishingPluginException {
        final List<String> args = new ArrayList<>();

        Options options = new Options();

        options.addOption(Option.builder("s").longOpt("stage").hasArg(true).argName("preprod").build());
        getArgValue(args, "--stage", () -> publication.getStage());

        options.addOption(Option.builder("h").longOpt("host").hasArg(true).argName("api-host").build());
        getArgValue(args, "--host", () -> publication.getHost());

        options.addOption(Option.builder("port").longOpt("port").hasArg(true).argName("8181").build());
        getArgValue(args, "--port", () -> String.valueOf(publication.getPort()));

        options.addOption(Option.builder("u").longOpt("username").hasArg(true).argName("apiadmin").build());
        getArgValue(args, "--username", () -> publication.getUsername());

        options.addOption(Option.builder("p").longOpt("password").hasArg(true).build());
        getArgValue(args, "--password", () -> publication.getPassword() );

        options.addOption(Option.builder("f").longOpt("force").hasArg(true).build());
        getArgValue(args, "--force", () -> String.valueOf(publication.getForce()));

        options.addOption(Option.builder("iq").longOpt("ignoreQuotas").hasArg(true).build());
        getArgValue(args, "--ignoreQuotas", () -> String.valueOf(publication.getIgnoreQuotas()));

        options.addOption(Option.builder("clientOrgsMode").longOpt("clientOrgsMode").hasArg(true).build());
        getArgValue(args, "--clientOrgsMode", () -> String.valueOf(publication.getClientOrgsMode()));

        options.addOption(Option.builder("clientAppsMode").longOpt("clientAppsMode").hasArg(true).build());
        getArgValue(args, "--clientAppsMode", () -> String.valueOf(publication.getClientAppsMode()));

        Options internalOptions = new Options();

        internalOptions
                .addOption(Option.builder("ignoreAdminAccount").longOpt("ignoreAdminAccount").hasArg(true).build());
        getArgValue(args, "--ignoreAdminAccount", () -> String.valueOf(publication.getIgnoreAdminAccount()));

        try {
            CommandLineParser parser = new RelaxedParser();
            final String[] argsValue = args.toArray(new String[args.size()]);
            final CommandLine cmd = parser.parse(options, argsValue);
            final CommandLine internalCmd = parser.parse(internalOptions, argsValue);
            CommandParameters params = new CommandParameters(cmd, internalCmd, new EnvironmentProperties(cmd.getOptionValue("stage"))); //NOSONAR
        } catch (AppException e) {
            logger.error(e);
            throw AppExceptionMapper.map(e);
        } catch (ParseException e) {
            throw new ArgumentParseException(e.getMessage(), e);
        }
    }

    /**
     * Processing <b>one</b> API by  calling the axway-swagger-promote tool
     *
     * @param e
     * @param stage
     *
     * @throws AppException
     */
    public void processApi(final Api e, final String stage) throws AppException {
        final APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();

        final APIImportConfigAdapter contract =
                new APIImportConfigAdapter(e.getApiConfig(), stage, e.getApiSpecification(),
                        apimAdapter.isUsingOrgAdmin());
        IAPI desiredAPI = contract.getDesiredAPI();
        IAPI actualAPI = apimAdapter.getAPIManagerAPI(apimAdapter.getExistingAPI(desiredAPI.getPath()), desiredAPI);
        APIChangeState changeActions = new APIChangeState(actualAPI, desiredAPI);
        apimAdapter.applyChanges(changeActions);
        logger.info("Successfully replicated API-State into API-Manager");
    }

    /**
     * Helper method to detect argument value.
     *
     * @param args
     *         a list of arguments defined by the axway-swagger-promote tool
     * @param option
     *         the option
     * @param supplier
     *         the callback function (getter method of the option)
     *
     * @return
     */
    private List getArgValue(final List args, String option, Supplier<String> supplier) {
        final String value = supplier.get();
        if (value != null && !"null".equals(value)) {
            args.add(option);
            args.add(value);
        }

        return args;
    }
}
