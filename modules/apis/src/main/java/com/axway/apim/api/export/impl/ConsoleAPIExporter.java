package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConsoleAPIExporter extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleAPIExporter.class);
    public static final String ID = "API-Id";
    public static final String PATH = "Path";
    public static final String NAME = "Name";
    public static final String VERSION = "Version";
    public static final String CREATED_ON = "Created-On";
    public static final String FORMAT = "%-25s";

    Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ConsoleAPIExporter(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        switch (params.getWide()) {
            case standard:
                printStandard(apis);
                break;
            case wide:
                printWide(apis);
                break;
            case ultra:
                printUltra(apis);
                break;
        }
    }

    private void printStandard(List<API> apis) {
        Console.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
                new Column().header(ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getId),
                new Column().header(PATH).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getPath),
                new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getName),
                new Column().header(VERSION).with(API::getVersion),
                new Column().header(CREATED_ON).with(this::getFormattedDate)
        )));
        printDetails(apis);
    }

    private void printWide(List<API> apis) {
        Console.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
                new Column().header(ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getId),
                new Column().header(PATH).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getPath),
                new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getName),
                new Column().header(VERSION).with(API::getVersion),
                new Column().header("V-Host").with(API::getVhost),
                new Column().header("State").with(this::getState),
                new Column().header("Backend").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(APIResultHandler::getBackendPath),
                new Column().header("Security").with(APIResultHandler::getUsedSecurity),
                new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxWidth(30).with(this::getUsedPoliciesForConsole),
                new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()),
                new Column().header(CREATED_ON).with(this::getFormattedDate)
        )));
        printDetails(apis);
    }

    private void printUltra(List<API> apis) {
        Console.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
                new Column().header(ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getId),
                new Column().header(PATH).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getPath),
                new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(API::getName),
                new Column().header(VERSION).with(API::getVersion),
                new Column().header("V-Host").with(API::getVhost),
                new Column().header("State").with(this::getState),
                new Column().header("Backend").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(APIResultHandler::getBackendPath),
                new Column().header("Security").with(APIResultHandler::getUsedSecurity),
                new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxWidth(30).with(this::getUsedPoliciesForConsole),
                new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()),
                new Column().header("Orgs").with(this::getOrgCount),
                new Column().header("Apps").with(this::getAppCount),
                new Column().header("Quotas").with(api -> Boolean.toString(hasQuota(api))),
                new Column().header("Tags").dataAlign(HorizontalAlign.LEFT).maxWidth(30).with(api -> Boolean.toString(hasTags(api))),
                new Column().header(CREATED_ON).with(this::getFormattedDate)
        )));
        printDetails(apis);
    }

    private String getFormattedDate(API api) {
        if (api.getCreatedOn() == null) return "N/A";
        return dateFormatter.format(api.getCreatedOn());
    }

    private String getUsedPoliciesForConsole(API api) {
        List<String> usedPolicies = new ArrayList<>();
        Map<PolicyType, List<String>> allPolicies = getUsedPolicies(api);
        for (List<String> policyNames : allPolicies.values()) {
            for (String polName : policyNames) {
                if (usedPolicies.contains(polName)) continue;
                usedPolicies.add(polName);
            }
        }
        return usedPolicies.toString().replace("[", "").replace("]", "");
    }

    private void printDetails(List<API> apis) {
        if (apis.size() != 1) return;
        API api = apis.get(0);

        // If wide isn't ultra, we have to reload some more information for the detail view
        if (!params.getWide().equals(Wide.ultra)) {
            try {
                APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
                apiAdapter.addClientApplications(api, new APIFilter.Builder().includeClientApplications(true).build());
                apiAdapter.addClientOrganizations(api, true);
                apiAdapter.addQuotaConfiguration(api, true);
            } catch (AppException e) {
                LOG.error("Error loading API details.", e);
            }
        }
        Console.println("A P I  -  D E T A I L S");
        Console.println(String.format(FORMAT, "Organization: ") + api.getOrganization().getName());
        Console.println(String.format(FORMAT, "Created On: ") + new Date(api.getCreatedOn()));
        Console.println(String.format(FORMAT, "Created By: ") + getCreatedBy(api));
        Console.println(String.format(FORMAT, "Granted Organizations: ") + getGrantedOrganizations(api).toString().replace("[", "").replace("]", ""));
        Console.println(String.format(FORMAT, "Subscribed applications: ") + getSubscribedApplications(api));
        Console.println(String.format(FORMAT, "Custom-Policies: ") + getUsedPolicies(api));
        Console.println(String.format(FORMAT, "Tags: ") + getTags(api));
        Console.println(String.format(FORMAT, "Custom-Properties: ") + getCustomProps(api));
    }

    private boolean hasQuota(API api) {
        return (api.getApplicationQuota() != null &&
                api.getApplicationQuota().getRestrictions() != null &&
                !api.getApplicationQuota().getRestrictions().isEmpty()) ||
                (api.getSystemQuota() != null &&
                        api.getSystemQuota().getRestrictions() != null &&
                        !api.getSystemQuota().getRestrictions().isEmpty())
                ;
    }

    private String getState(API api) {
        return api.getState();
    }

    private String getCreatedBy(API api) {
        try {
            return APIManagerAdapter.getInstance().getUserAdapter().getUserForId(api.getCreatedBy()).getName();
        } catch (Exception e) {
            LOG.error("Error getting created by user", e);
            return "Err";
        }
    }


    private boolean hasTags(API api) {
        return (api.getTags() != null && !api.getTags().isEmpty());
    }

    private String getOrgCount(API api) {
        if (api.getClientOrganizations() == null) return "N/A";
        return Integer.toString(api.getClientOrganizations().size());
    }

    private String getAppCount(API api) {
        if (api.getApplications() == null) return "N/A";
        return Integer.toString(api.getApplications().size());
    }

    private String getSubscribedApplications(API api) {
        if (api.getApplications() == null) return "N/A";
        List<String> subscribedApps = new ArrayList<>();
        for (ClientApplication app : api.getApplications()) {
            subscribedApps.add(app.getName());
        }
        return subscribedApps.toString().replace("[", "").replace("]", "");
    }

    @Override
    public APIFilter getFilter() {
        return createFilter();
    }
}
