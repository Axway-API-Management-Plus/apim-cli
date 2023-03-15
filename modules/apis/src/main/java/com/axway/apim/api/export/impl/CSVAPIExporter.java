package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
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

    DateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private enum HeaderFields {
        standard(new String[]{
                "API ID",
                "API Name",
                "API Path",
                "API Version",
                "Created on"
        }),
        wide(new String[]{
                "API ID",
                "API Organization",
                "API Name",
                "API Path",
                "API Version",
                "API V-Host",
                "API State",
                "Backend",
                "Request Policy",
                "Routing Policy",
                "Response Policy",
                "Fault-Handler Policy",
                "Created on"
        }),
        ultra(new String[]{
                "API ID",
                "API Organization",
                "API Name",
                "API Path",
                "API Version",
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
                "Application Organization",
                "Created on"
        });

        final String[] headerFields;

        HeaderFields(String[] headerFields) {
            this.headerFields = headerFields;
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
                try (CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(HeaderFields.valueOf(wide.name()).headerFields))) {
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
                getFormattedDate(api)
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
                getFormattedDate(api)
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
                app.getOrganization().getName(),
                getFormattedDate(api)
        );
    }

    private String getFormattedDate(API api) {
        if (api.getCreatedOn() == null) return "N/A";
        return isoDateFormatter.format(api.getCreatedOn());
    }

    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder();

        switch (params.getWide()) {
            case standard:
            case wide:
                builder.includeQuotas(false);
                builder.includeClientApplications(false);
                builder.includeClientOrganizations(false);
                builder.includeClientAppQuota(false);
                builder.includeQuotas(false);
                break;
            case ultra:
                builder.includeQuotas(true);
                builder.includeClientAppQuota(false);
                builder.includeClientApplications(true);
                builder.includeClientOrganizations(true);
                break;
        }
        return builder.build();
    }
}
