package com.axway.apim.appexport.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.APIAccessComparator;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appexport.lib.ApplicationComparator;
import com.axway.apim.lib.ExportResult;
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
import java.util.List;

public class CSVAppExporter extends ApplicationExporter {
    public static final String PHONE = "Phone";
    private static final Logger LOG = LoggerFactory.getLogger(CSVAppExporter.class);
    public static final String ID = "ID";
    public static final String ORGANIZATION = "Organization";
    public static final String NAME = "Name";
    public static final String EMAIL = "Email";
    public static final String STATE = "State";
    public static final String ENABLED = "Enabled";
    public static final String CREATED_BY = "Created by";

    private enum HeaderFields {
        standard(new String[]{
            ID,
            ORGANIZATION,
            NAME,
            EMAIL,
            PHONE,
            STATE,
            ENABLED,
            CREATED_BY
        }),
        wide(new String[]{
            ID,
            ORGANIZATION,
            NAME,
            EMAIL,
            PHONE,
            STATE,
            ENABLED,
            CREATED_BY,
            "API Quota",
            "API-Method",
            "Quota Config"
        }),
        ultra(new String[]{
            ID,
            ORGANIZATION,
            NAME,
            EMAIL,
            PHONE,
            STATE,
            ENABLED,
            CREATED_BY,
            "API-Name",
            "API-Version",
            "Access created by",
            "Access created on"
        });

        final String[] fields;

        HeaderFields(String[] headerFields) {
            this.fields = headerFields;
        }
    }

    private final APIManagerAPIAdapter apiAdapter;
    private final APIManagerAPIMethodAdapter methodAdapter;

    public CSVAppExporter(AppExportParams params, ExportResult result) throws AppException {
        super(params, result);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        apiAdapter = apiManagerAdapter.getApiAdapter();
        methodAdapter = apiManagerAdapter.getMethodAdapter();
    }

    @Override
    public void export(List<ClientApplication> apps) throws AppException {
        Wide wide = params.getWide();
        String givenTarget = params.getTarget();
        try {
            File target = new File(givenTarget);
            if (target.isDirectory()) {
                target = new File(givenTarget + File.separator + Utils.createFileName(params.getAPIManagerURL().getHost(), params.getStage(), "app_export_"));
            }
            if (target.exists() && !params.isDeleteTarget()) {
                throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS);
            }
            try (FileWriter appendable = new FileWriter(target)) {
                try (CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.Builder.create().setHeader(HeaderFields.valueOf(wide.name()).fields).build())) {
                    writeRecords(csvPrinter, apps, wide);
                    LOG.info("Application export successfully written to file: {}", target.getCanonicalPath());
                }
            }
        } catch (IOException e1) {
            throw new AppException("Cant open CSV-File: " + givenTarget + " for writing", ErrorCode.UNXPECTED_ERROR, e1);
        }
    }

    private void writeRecords(CSVPrinter csvPrinter, List<ClientApplication> apps, Wide wide) throws IOException {
        apps.sort(new ApplicationComparator());
        int i = 0;
        for (ClientApplication app : apps) {
            if (i % 50 == 0) {
                csvPrinter.flush();
            }
            // With wide - Report the application quotas
            if (wide.equals(Wide.wide)) {
                if (app.getAppQuota() != null && app.getAppQuota().getRestrictions() != null && !app.getAppQuota().getRestrictions().isEmpty()) {
                    for (QuotaRestriction restriction : app.getAppQuota().getRestrictions()) {
                        writeRecords(csvPrinter, app, null, restriction, wide);
                    }
                } else {
                    writeRecords(csvPrinter, app, null, null, wide);
                }

                // With ultra - Report all subscribed APIs
            } else if (wide.equals(Wide.ultra)) {
                if (app.getApiAccess() != null)
                    app.getApiAccess().sort(new APIAccessComparator());
                for (APIAccess apiAccess : app.getApiAccess()) {
                    writeRecords(csvPrinter, app, apiAccess, null, wide);
                }
            } else {
                writeRecords(csvPrinter, app, null, null, wide);
            }
            i++;
        }
        csvPrinter.flush();
    }

    private void writeRecords(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess, QuotaRestriction restriction, Wide wide) throws IOException {
        switch (wide) {
            case standard:
                writeStandardToCSV(csvPrinter, app);
                break;
            case wide:
                writeWideToCSV(csvPrinter, app, restriction);
                break;
            case ultra:
                writeUltraToCSV(csvPrinter, app, apiAccess);
                break;
            default:
                break;
        }
    }

    private void writeStandardToCSV(CSVPrinter csvPrinter, ClientApplication app) throws IOException {
        csvPrinter.printRecord(
            app.getId(),
            app.getOrganization().getName(),
            app.getName(),
            app.getEmail(),
            app.getPhone(),
            app.getState(),
            app.isEnabled(),
            getCreatedBy(app.getCreatedBy(), app)
        );
    }

    private void writeWideToCSV(CSVPrinter csvPrinter, ClientApplication app, QuotaRestriction quotaRestriction) throws IOException {
        csvPrinter.printRecord(
            app.getId(),
            app.getOrganization().getName(),
            app.getName(),
            app.getEmail(),
            app.getPhone(),
            app.getState(),
            app.isEnabled(),
            getCreatedBy(app.getCreatedBy(), app),
            getRestrictedAPI(quotaRestriction),
            getRestrictedMethod(quotaRestriction),
            getQuotaConfig(quotaRestriction)
        );
    }

    private void writeUltraToCSV(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess) throws IOException {
        csvPrinter.printRecord(
            app.getId(),
            app.getOrganization().getName(),
            app.getName(),
            app.getEmail(),
            app.getPhone(),
            app.getState(),
            app.isEnabled(),
            getCreatedBy(app.getCreatedBy(), app),
            apiAccess.getApiName(),
            apiAccess.getApiVersion(),
            getCreatedBy(apiAccess.getCreatedBy(), app),
            getCreatedOn(apiAccess.getCreatedOn())
        );
    }

    private String getRestrictedAPI(QuotaRestriction quotaRestriction) throws AppException {
        if (quotaRestriction == null) return "N/A";
        API api = apiAdapter.getAPIWithId(quotaRestriction.getApiId());
        if (api == null) return "Err";
        return api.getName();
    }

    private String getRestrictedMethod(QuotaRestriction quotaRestriction) throws AppException {
        if (quotaRestriction == null) return "N/A";
        API restrictedAPI = apiAdapter.getAPIWithId(quotaRestriction.getApiId());
        if (restrictedAPI == null) return "Err";
        return quotaRestriction.getMethod().equals("*") ? "All Methods" : methodAdapter.getMethodForId(restrictedAPI.getId(), quotaRestriction.getMethod()).getName();
    }

    private String getQuotaConfig(QuotaRestriction quotaRestriction) {
        if (quotaRestriction == null) return "N/A";
        return "" + quotaRestriction.getConfig();
    }

    @Override
    public ClientAppFilter getFilter() throws AppException {
        Builder builder = getBaseFilterBuilder();

        switch (params.getWide()) {
            case standard:
                break;
            case wide:
                builder.includeQuotas(true);
                break;
            case ultra:
                builder.includeAPIAccess(true);
                break;
        }
        return builder.build();
    }
}
