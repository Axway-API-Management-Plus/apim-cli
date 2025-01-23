package com.axway.apim.organization.impl;

import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.Constants;
import com.axway.apim.organization.lib.OrgExportParams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvOrgExporter extends OrgResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CsvOrgExporter.class);

    protected CsvOrgExporter(OrgExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<Organization> organizations) throws AppException {
        StandardExportParams.Wide wide = params.getWide();
        String givenTarget = params.getTarget();
        try {
            File target = new File(givenTarget);
            if (target.isDirectory()) {
                target = new File(givenTarget + File.separator + Utils.createFileName(params.getAPIManagerURL().getHost(), params.getStage(), "org_export_"));
            }
            if (target.exists() && !params.isDeleteTarget()) {
                throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS);
            }
            try (FileWriter appendable = new FileWriter(target)) {
                try (CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.Builder.create().setHeader(HeaderFields.valueOf(wide.name()).fields).build())) {
                    writeRecords(csvPrinter, organizations, wide);
                    LOG.info("Organization export successfully written to file: {}", target.getCanonicalPath());
                }
            }
        } catch (IOException e1) {
            throw new AppException("Cant open CSV-File: " + givenTarget + " for writing", ErrorCode.UNXPECTED_ERROR, e1);
        }
    }

    @Override
    public OrgFilter getFilter() throws AppException {
        return new OrgFilter.Builder()
            .hasId(params.getId())
            .hasDevelopment(params.getDev())
            .includeCustomProperties(getCustomProperties())
            .hasName(params.getName()).build();
    }

    private enum HeaderFields {
        standard(new String[]{
            Constants.ORGANIZATION_ID,
            Constants.NAME,
            Constants.EMAIL,
            Constants.PHONE,
            Constants.VIRTUAL_HOST,
            Constants.ENABLED,
            Constants.DEV,
            Constants.CREATED_ON
        }),
        wide(new String[]{
            Constants.ORGANIZATION_ID,
            Constants.NAME,
            Constants.EMAIL,
            Constants.PHONE,
            Constants.VIRTUAL_HOST,
            Constants.VIRTUAL_HOST,
            Constants.ENABLED,
            Constants.DEV,
            Constants.CREATED_ON,
            "Custom-Properties"
        }),
        ultra(new String[]{
            Constants.ORGANIZATION_ID,
            Constants.NAME,
            Constants.EMAIL,
            Constants.PHONE,
            Constants.VIRTUAL_HOST,
            Constants.VIRTUAL_HOST,
            Constants.ENABLED,
            Constants.DEV,
            Constants.CREATED_ON,
            "Custom-Properties",
            "API-Name",
            "API-Version",
            "State",
            "API Status"
        });

        final String[] fields;

        HeaderFields(String[] headerFields) {
            this.fields = headerFields;
        }
    }

    private void writeRecords(CSVPrinter csvPrinter, List<Organization> organizations, StandardExportParams.Wide wide) throws IOException {
        int i = 0;
        for (Organization organization : organizations) {
            if (i % 50 == 0) {
                csvPrinter.flush();
            }
            // With wide - Report the application quotas
            if (wide.equals(StandardExportParams.Wide.wide)) {
                // With ultra - Report all subscribed APIs
            } else if (wide.equals(StandardExportParams.Wide.ultra)) {
                for (APIAccess apiAccess : organization.getApiAccess()) {
                    writeRecords(csvPrinter, organization, apiAccess, wide);
                }
            } else {
                writeRecords(csvPrinter, organization, null, wide);
            }
            i++;
        }
        csvPrinter.flush();
    }

    private void writeRecords(CSVPrinter csvPrinter, Organization organization, APIAccess apiAccess, StandardExportParams.Wide wide) throws IOException {
        switch (wide) {
            case standard:
                writeStandardToCSV(csvPrinter, organization);
                break;
            case wide:
                writeWideToCSV(csvPrinter, organization, null);
                break;
            case ultra:
                writeUltraToCSV(csvPrinter, organization, apiAccess);
                break;
            default:
                break;
        }
    }

    private void writeStandardToCSV(CSVPrinter csvPrinter, Organization organization) throws IOException {
        csvPrinter.printRecord(
            organization.getId(),
            organization.getName(),
            organization.getEmail(),
            organization.getPhone(),
            organization.isEnabled()
        );
    }

    private void writeWideToCSV(CSVPrinter csvPrinter, Organization organization, QuotaRestriction quotaRestriction) throws IOException {
        csvPrinter.printRecord(
            organization.getId(),
            organization.getName(),
            organization.getEmail(),
            organization.getPhone(),
            organization.isEnabled()
        );
    }

    private void writeUltraToCSV(CSVPrinter csvPrinter, Organization organization, APIAccess apiAccess) throws IOException {
        csvPrinter.printRecord(
            organization.getId(),
            organization.getName(),
            organization.getEmail(),
            organization.getPhone(),
            organization.isEnabled(),
            apiAccess.getApiName(),
            apiAccess.getApiVersion()
        );
    }

}
