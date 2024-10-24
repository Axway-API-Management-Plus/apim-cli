package com.axway.apim.organization.impl;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.organization.lib.ExportOrganization;
import com.axway.apim.organization.lib.OrgExportParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class YamlOrgExporter extends JsonOrgExporter {
    private static final Logger LOG = LoggerFactory.getLogger(YamlOrgExporter.class);

    public YamlOrgExporter(OrgExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<Organization> orgs) throws AppException {
        LOG.info("Exporting organizations in yaml format");
        for (Organization org : orgs) {
            saveOrganizationLocally(new ObjectMapper(CustomYamlFactory.createYamlFactory()), new ExportOrganization(org), "/org-config.yaml");
        }
    }
}
