package com.axway.apim.organization.impl;

import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.jackson.ImageSerializer;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.lib.ExportOrganization;
import com.axway.apim.organization.lib.OrgExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class JsonOrgExporter extends OrgResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonOrgExporter.class);

    public JsonOrgExporter(OrgExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<Organization> orgs) throws AppException {
        for (Organization org : orgs) {
            saveOrganizationLocally(new ObjectMapper(), new ExportOrganization(org), "/org-config.json");
        }
    }

    public void saveOrganizationLocally(ObjectMapper mapper, ExportOrganization org, String configFile) throws AppException {
        File localFolder = null;
        if (!EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            String folderName = getExportFolder(org);
            String targetFolder = params.getTarget();
            localFolder = new File(targetFolder + File.separator + folderName);
            LOG.info("Going to export organizations into folder: {}", localFolder);
            if (localFolder.exists()) {
                if (OrgExportParams.getInstance().isDeleteTarget()) {
                    Utils.deleteDirectory(localFolder);
                } else {
                    LOG.warn("Local export folder: {} already exists. Organization will not be exported. (You may set -deleteTarget)", localFolder);
                    this.hasError = true;
                    return;
                }
            }
            if (!localFolder.mkdirs()) {
                throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
            }
        }
        mapper.registerModule(new SimpleModule().addSerializer(Image.class, new ImageSerializer()));
        FilterProvider filters = new SimpleFilterProvider()
            .addFilter("OrganizationFilter", SimpleBeanPropertyFilter.serializeAllExcept("id", "dn"))
            .addFilter("APIAccessFilter", SimpleBeanPropertyFilter.filterOutAllExcept("apiName", "apiVersion"))
            .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept("createdOn"));
        mapper.setFilterProvider(filters);
        mapper.setSerializationInclusion(Include.NON_NULL);
        writeContent(org, mapper, localFolder, configFile);
        if (org.getImage() != null && !EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            writeBytesToFile(org.getImage().getImageContent(), localFolder + File.separator + org.getImage().getBaseFilename());
        }
        LOG.info("Successfully exported organization into folder: {}", localFolder);
    }

    public void writeContent(ExportOrganization org, ObjectMapper mapper, File localFolder, String configFile) throws AppException {
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                mapper.writeValue(System.out, org);
            } else {
                mapper.writeValue(new File(localFolder.getCanonicalPath() + configFile), org);
            }
            this.result.addExportedFile((localFolder != null ? localFolder.getCanonicalPath() : null) + configFile);
        } catch (Exception e) {
            throw new AppException("Can't write configuration file for organization: '" + org.getName() + "'", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    private String getExportFolder(ExportOrganization org) {
        return Utils.replaceSpecialChars(org.getName());
    }

    @Override
    public OrgFilter getFilter() {
        return getBaseOrgFilterBuilder()
            .includeImage(true)
            .includeAPIAccess(true)
            .build();
    }

    public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileDest)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
