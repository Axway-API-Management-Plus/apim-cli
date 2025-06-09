package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIComparator;
import com.axway.apim.api.export.lib.ClientAppComparator;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CSVAPIExporter extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CSVAPIExporter.class);
    public static final String API_ID = "API ID";
    public static final String API_NAME = "API Name";
    public static final String API_PATH = "API Path";
    public static final String API_VERSION = "API Version";
    public static final String CREATED_ON = "Created on";

    DateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private enum HeaderFields {
        standard(new String[]{
            API_ID,
            API_NAME,
            API_PATH,
            API_VERSION,
            CREATED_ON
        }),
        wide(new String[]{
            API_ID,
                "API Organization",
            API_NAME,
            API_PATH,
            API_VERSION,
                "API V-Host",
                "API State",
                "Backend",
                "Request Policy",
                "Routing Policy",
                "Response Policy",
                "Fault-Handler Policy",
            CREATED_ON
        }),
        ultra(new String[]{
            API_ID,
                "API Organization",
            API_NAME,
            API_PATH,
            API_VERSION,
                "API V-Host",
                "API State",
                "Backend",
                "Security",
                "Request Policy",
                "Routing Policy",
                "Response Policy",
                "Fault-Handler Policy",
                "Custom-Properties",
                "Tags",
                "Granted Organization",
                "Application Name",
                "Application ID",
                "Application Organization",
                "Application Created On",
            CREATED_ON
        });

        final String[] fields;

        HeaderFields(String[] headerFields) {
            this.fields = headerFields;
        }
    }

    public CSVAPIExporter(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        Wide wide = params.getWide();
        String givenTarget = params.getTarget();
        try {
            File target = new File(givenTarget);
            if (target.isDirectory()) {
                target = new File(givenTarget + File.separator + Utils.createFileName(params.getAPIManagerURL().getHost(), params.getStage(), "api_export_"));
            }
            if (target.exists() && !params.isDeleteTarget()) {
                throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS);
            }
            try (FileWriter appendable = new FileWriter(target)) {
                try (CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.Builder.create().setHeader(HeaderFields.valueOf(wide.name()).fields).build())) {
                    writeRecords(csvPrinter, apis, wide);
                    LOG.info("API export successfully written to file: {}", target.getCanonicalPath());
                }
            }
        } catch (IOException e1) {
            throw new AppException("Cant open CSV-File: " + givenTarget + " for writing", ErrorCode.UNXPECTED_ERROR, e1);
        }
    }

    private void writeRecords(CSVPrinter csvPrinter, List<API> apis, Wide wide) throws IOException {
        apis.sort(new APIComparator());
        int i = 0;
        for (API api : apis) {
            if (i % 50 == 0) {
                csvPrinter.flush();
            }
            // Don't show Orgs and App if view is not ULTRA
            if (wide.equals(Wide.ultra)) {
                if (api.getApplications() != null) api.getApplications().sort(new ClientAppComparator());
                for (Organization org : getClientOrganizations(api)) {
                    for (ClientApplication app : getClientApplications(api)) {
                        writeRecords(csvPrinter, api, app, org, wide);
                        if (i % 50 == 0) {
                            csvPrinter.flush();
                        }
                    }
                    if (i % 50 == 0) {
                        csvPrinter.flush();
                    }
                }
            } else {
                writeRecords(csvPrinter, api, null, null, wide);
            }
        }
        csvPrinter.flush();
    }

    private List<Organization> getClientOrganizations(API api) {
        if (api.getClientOrganizations() == null || api.getClientOrganizations().isEmpty()) {
            Organization org = new Organization();
            org.setName("N/A");
            List<Organization> ungranted = new ArrayList<>();
            ungranted.add(org);
            return ungranted;
        } else {
            return api.getClientOrganizations();
        }
    }

    private List<ClientApplication> getClientApplications(API api) {
        if (api.getApplications() == null || api.getApplications().isEmpty()) {
            ClientApplication app = new ClientApplication();
            app.setName("N/A");
            Organization org = new Organization();
            org.setName("N/A");
            app.setOrganization(org);
            List<ClientApplication> subscribed = new ArrayList<>();
            subscribed.add(app);
            return subscribed;
        } else {
            return api.getApplications();
        }
    }

    private void writeRecords(CSVPrinter csvPrinter, API api, ClientApplication app, Organization org, Wide wide) throws IOException {
        switch (wide) {
            case standard:
                writeStandardToCSV(csvPrinter, api);
                break;
            case wide:
                writeWideToCSV(csvPrinter, api);
                break;
            case ultra:
                writeAPIUltraToCSV(csvPrinter, api, app, org);
                break;
            default:
                break;
        }
    }

    private void writeStandardToCSV(CSVPrinter csvPrinter, API api) throws IOException {
        csvPrinter.printRecord(
                api.getId(),
                api.getName(),
                api.getPath(),
                api.getVersion(),
                getFormattedDate(api.getCreatedOn())
        );
    }

    private void writeWideToCSV(CSVPrinter csvPrinter, API api) throws IOException {
        csvPrinter.printRecord(
                api.getId(),
                api.getOrganization().getName(),
                api.getName(),
                api.getPath(),
                api.getVersion(),
                api.getVhost(),
                api.getState(),
                getBackendPath(api),
                getUsedPolicies(api, PolicyType.REQUEST).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.ROUTING).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.RESPONSE).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.FAULT_HANDLER).toString().replace("[", "").replace("]", ""),
                getFormattedDate(api.getCreatedOn())
        );
    }

    private void writeAPIUltraToCSV(CSVPrinter csvPrinter, API api, ClientApplication app, Organization org) throws IOException {
        csvPrinter.printRecord(
                api.getId(),
                api.getOrganization().getName(),
                api.getName(),
                api.getPath(),
                api.getVersion(),
                api.getVhost(),
                api.getState(),
                getBackendPath(api),
                getUsedSecurity(api),
                getUsedPolicies(api, PolicyType.REQUEST).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.ROUTING).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.RESPONSE).toString().replace("[", "").replace("]", ""),
                getUsedPolicies(api, PolicyType.FAULT_HANDLER).toString().replace("[", "").replace("]", ""),
                getCustomProps(api),
                getTags(api),
                org.getName(),
                app.getName(),
                app.getId(),
                app.getOrganization().getName(),
                getFormattedDate(app.getCreatedOn()),
                getFormattedDate(api.getCreatedOn())
        );
    }

    private String getFormattedDate(Long longDate) {
        if (longDate == null) return "N/A";
        return isoDateFormatter.format(longDate);
    }

    @Override
    public APIFilter getFilter() {
        return createFilter();
    }
}
