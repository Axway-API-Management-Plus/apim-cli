package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIFilterParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class CLIAPIFilterOptions extends CLIOptions {

    private static final Logger LOG = LoggerFactory.getLogger(CLIAPIFilterOptions.class);

    private final CLIOptions cliOptions;

    public CLIAPIFilterOptions(CLIOptions cliOptions) {
        this.cliOptions = cliOptions;
    }

    @Override
    public Parameters getParams() throws AppException {
        APIFilterParams params = (APIFilterParams) cliOptions.getParams();
        params.setApiPath(getValue("a"));
        params.setName(getValue("n"));
        params.setOrganization(getValue("org"));
        params.setId(getValue("id"));
        params.setPolicy(getValue("policy"));
        params.setVhost(getValue("vhost"));
        params.setState(getValue("state"));
        params.setBackend(getValue("backend"));
        params.setTag(getValue("tag"));
        params.setInboundSecurity(getValue("inboundsecurity"));
        params.setOutboundAuthentication(getValue("outboundauthn"));
        parseCreatedOnFilter(params);
        return (Parameters) params;
    }

    private void parseCreatedOnFilter(APIFilterParams params) throws AppException {
        try {
            List<String> dateFormats = Arrays.asList("yyyy-MM-dd", "yyyy-MM", "yyyy"); // "dd.MM.yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"
            String createdOn = getValue("createdOn");
            if (createdOn == null) return;
            String[] createdOns = createdOn.trim().toLowerCase().split(":");
            if (createdOns.length != 2) {
                throw new AppException("You must separate the start- and end-date with a ':'.", ErrorCode.INVALID_PARAMETER);
            }
            String start = createdOns[0];
            String end = createdOns[1];
            if (start.equals("now")) {
                throw new AppException("You cannot use 'now' as the start date.", ErrorCode.INVALID_PARAMETER);
            }
            Date startDate = null;
            Date endDate = null;
            for (String pattern : dateFormats) {
                startDate = parseDate(start, pattern, 1);
                if (startDate != null) break;
            }
            for (String pattern : dateFormats) {
                endDate = parseDate(end, pattern, 2);
                if (endDate != null) break;
            }
            if (startDate == null || endDate == null) {
                throw new AppException("Unable to parse given createdOn filter: '" + createdOn + "'", ErrorCode.INVALID_PARAMETER);
            }
            if (startDate.getTime() > endDate.getTime()) {
                SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss", Locale.ENGLISH);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                throw new AppException("The start-date: " + df.format(startDate) + " GMT cannot be bigger than the end date: " + df.format(endDate) + " GMT.", ErrorCode.INVALID_PARAMETER);
            }
            params.setCreatedOnAfter("" + startDate.getTime());
            params.setCreatedOnBefore("" + endDate.getTime());
        } catch (Exception e) {
            LOG.info("Valid createdOn filter examples: 2020-01-01:2020-12-31   2020:2021   2020-06:now");
            throw e;
        }
    }

    private static Date parseDate(String inputDate, String pattern, int endOrStart) {
        if (inputDate.equals("now")) return new Date();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = df.parse(inputDate);
            if (date == null) return null;
            cal.setTime(date);
            if (endOrStart == 2) { // 1 = End Date - Set some defaults
                if (inputDate.length() == 4) { // yyyy - Missing day and month
                    cal.set(Calendar.DAY_OF_MONTH, 31);
                    cal.set(Calendar.MONTH, 11);
                } else if (inputDate.length() == 7 || inputDate.length() == 6) { // No day given (yyyy-MM or yyyy-M)
                    cal.set(Calendar.DAY_OF_MONTH, 31);
                }
                // For the endDate we always have to shift the time to the end of the day
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
            }
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void parse() throws AppException {
        cliOptions.parse();
    }

    @Override
    public void addOption(Option option) {
        cliOptions.addOption(option);
    }


    @Override
    public String getValue(String key) {
        return cliOptions.getValue(key);
    }

    @Override
    public boolean hasOption(String key) {
        return cliOptions.hasOption(key);
    }

    @Override
    public void printUsage(String message, String[] args) {
        cliOptions.printUsage(message, args);
    }

    @Override
    public void showReturnCodes() {
        cliOptions.showReturnCodes();
    }

    @Override
    public void addOptions() {
        cliOptions.addOptions();
        Option option = new Option("a", "api-path", true, "Filter APIs to be exported, based on the exposure path.\n"
            + "You can use wildcards to export multiple APIs:\n"
            + "-a /api/v1/my/great/api     : Export a specific API\n"
            + "-a *                        : Export all APIs\n"
            + "-a /api/v1/any*             : Export all APIs with this prefix\n"
            + "-a */some/other/api         : Export APIs end with the same path\n");
        option.setRequired(false);
        option.setArgName("/api/v1/my/great/api");
        cliOptions.addOption(option);

        option = new Option("n", "name", true, "Filter APIs with the given name. Wildcards at the beginning/end are supported.");
        option.setRequired(false);
        option.setArgName("*MyName*");
        cliOptions.addOption(option);

        option = new Option("org", true, "Filter APIs with the given organization. Wildcards at the beginning/end are supported.");
        option.setRequired(false);
        option.setArgName("*MyOrg*");
        cliOptions.addOption(option);

        option = new Option("id", true, "Filter the API with that specific ID.");
        option.setRequired(false);
        option.setArgName("UUID-ID-OF-THE-API");
        cliOptions.addOption(option);

        option = new Option("policy", true, "Filter APIs with the given policy name. This is includes all policy types.");
        option.setRequired(false);
        option.setArgName("*Policy1*");
        cliOptions.addOption(option);

        option = new Option("vhost", true, "Filter APIs with that specific virtual host.");
        option.setRequired(false);
        option.setArgName("vhost.customer.com");
        cliOptions.addOption(option);

        option = new Option("state", true, "Filter APIs with specific state: unpublished | pending | published");
        option.setRequired(false);
        option.setArgName("published");
        cliOptions.addOption(option);

        option = new Option("backend", true, "Filter APIs with specific backendBasepath. Wildcards are supported.");
        option.setRequired(false);
        option.setArgName("*mybackhost.com*");
        cliOptions.addOption(option);

        option = new Option("createdOn", true, "Filter APIs based on their creation date. It's a range start:end. You see more examples when you provide an invalid range");
        option.setRequired(false);
        option.setArgName("2020-08:now");
        cliOptions.addOption(option);

        option = new Option("inboundsecurity", true, "Filter APIs with specific Inbound-Security. Wildcards are supported when filtering for APIs using a custom security policy.");
        option.setRequired(false);
        option.setArgName("oauth-ext|api-key|*my-security-pol*|...");
        cliOptions.addOption(option);

        option = new Option("outboundauthn", true, "Filter APIs with specific Outbound-Authentication. Wildcards are supported when filtering for an OAuth Provider profile.");
        option.setRequired(false);
        option.setArgName("oauth|api-key|My provider profile*|...");
        cliOptions.addOption(option);

        option = new Option("tag", true, "Filter APIs with a specific tag. Use either \"*myTagValueOrGroup*\" or \"tagGroup=*myTagValue*\"");
        option.setRequired(false);
        option.setArgName("tagGroup=*myTagValue*");
        cliOptions.addOption(option);
    }

    @Override
    public EnvironmentProperties getEnvProperties() {
        return cliOptions.getEnvProperties();
    }
}
