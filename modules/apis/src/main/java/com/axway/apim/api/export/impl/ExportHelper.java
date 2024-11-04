package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.jackson.serializer.APIExportSerializerModifier;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.AuthType;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.WSDLSpecification;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ExportHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExportHelper.class);

    private final APIExportParams params;

    public ExportHelper(APIExportParams params) {
        this.params = params;
    }

    public void saveAPILocally(ObjectMapper mapper, ExportAPI exportAPI, String configFile) throws AppException {

        String apiPath = getAPIExportFolder(exportAPI.getPath());
        File localFolder = new File(params.getTarget() + File.separator + getVHost(exportAPI) + apiPath);
        if (!EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            LOG.debug("Going to export API: {} into folder: {} ", exportAPI.getName(), localFolder);
            validateFolder(localFolder);
        }
        APISpecification apiDef = exportAPI.getAPIDefinition();
        // Skip processing if API definition is not available due to original API cloned and deleted.
        if (apiDef == null) {
            LOG.error("Backend API Definition is not available for the API : {}, hence use the option -useFEAPIDefinition to export API", exportAPI.getName());
            if (params.getId() != null)
                throw new AppException("Backend API Definition is not available for the API : " + exportAPI.getName() + ", hence use the option -useFEAPIDefinition to export API", ErrorCode.BACKEND_API_DEF_NA);
            return;
        }
        String targetFile = null;
        try {
            String fileName = Utils.replaceSpecialChars(exportAPI.getName()) + apiDef.getAPIDefinitionType().getFileExtension();
            targetFile = localFolder.getCanonicalPath() + "/" + fileName;
            if (!(apiDef instanceof WSDLSpecification && EnvironmentProperties.RETAIN_BACKEND_URL) && (!EnvironmentProperties.PRINT_CONFIG_CONSOLE)) {
                Object spec = mapper.readValue(apiDef.getApiSpecificationContent(), Object.class);
                mapper.writerWithDefaultPrettyPrinter().writeValue(new File(targetFile), spec);
                exportAPI.getAPIDefinition().setApiSpecificationFile(fileName);
            }
        } catch (IOException e) {
            throw new AppException("Can't save API-Definition locally to file: " + targetFile, ErrorCode.UNXPECTED_ERROR, e);
        }
        Image image = exportAPI.getAPIImage();
        if (image != null && (!EnvironmentProperties.PRINT_CONFIG_CONSOLE)) {
            writeBytesToFile(image.getImageContent(), localFolder + File.separator + image.getBaseFilename());
        }
        if (exportAPI.getCaCerts() != null && !exportAPI.getCaCerts().isEmpty()) {
            storeCaCerts(localFolder, exportAPI.getCaCerts());
        }

        storePrivateCerts(localFolder, exportAPI.getAuthenticationProfiles());

        mapper.registerModule(new SimpleModule().setSerializerModifier(new APIExportSerializerModifier()));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FilterProvider filters = new SimpleFilterProvider()
            .addFilter("CaCertFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("inbound", "outbound", "certFile"))
            .addFilter("ProfileFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("apiMethodId"))
            .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept());
        mapper.setFilterProvider(filters);
        writeContent(mapper, exportAPI, localFolder, configFile);
        LOG.info("Successfully exported API: {} into folder: {}", exportAPI.getName(), localFolder.getAbsolutePath());
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) {
            LOG.warn("Export has been done with an Org-Admin account only. Export is restricted by the following: ");
            LOG.warn("- No Quotas has been exported for the API");
            LOG.warn("- No Client-Organizations");
            LOG.warn("- Only subscribed applications from the Org-Admins organization");
        }
    }

    private String getVHost(ExportAPI exportAPI) throws AppException {
        if (exportAPI.getVhost() != null) return exportAPI.getVhost().replace(":", "_") + File.separator;
        String orgVHost = APIManagerAdapter.getInstance().getOrgAdapter().getOrg(new OrgFilter.Builder().hasId(exportAPI.getOrganizationId()).build()).getVirtualHost();
        if (orgVHost != null) return orgVHost.replace(":", "_") + File.separator;
        return "";
    }

    private void storeCaCerts(File localFolder, List<CaCert> caCerts) throws AppException {
        Base64.Decoder decoder = Base64.getDecoder();
        Base64.Encoder encoder = Base64.getMimeEncoder(64, System.lineSeparator().getBytes());
        for (CaCert caCert : caCerts) {
            if (caCert.getCertBlob() == null) {
                LOG.warn("- Ignoring cert export for null certBlob for alias: {}", caCert.getAlias());
            } else {
                if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                    final String encodedCertText = "data:application/x-pem-file;base64," + caCert.getCertBlob();
                    caCert.setCertFile(encodedCertText);
                } else {
                    String filename = caCert.getCertFile();
                    final String encodedCertText = new String(encoder.encode(decoder.decode(caCert.getCertBlob())));
                    byte[] certContent = ("-----BEGIN CERTIFICATE-----\n" + encodedCertText + "\n-----END CERTIFICATE-----").getBytes();
                    try {
                        writeBytesToFile(certContent, localFolder + "/" + filename);
                    } catch (AppException e) {
                        throw new AppException("Can't write certificate to disc", ErrorCode.UNXPECTED_ERROR, e);
                    }
                }
            }
        }
    }

    private void storePrivateCerts(File localFolder, List<AuthenticationProfile> authenticationProfiles) throws AppException {
        for (AuthenticationProfile profile : authenticationProfiles) {
            if (profile.getType() == AuthType.ssl && !EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                try {
                    Map<String, Object> parameters = profile.getParameters();
                    String pfx = (String) parameters.get("pfx");
                    String content = pfx.replaceFirst("data:.+,", "");
                    byte[] data = Base64.getMimeDecoder().decode(content.replace("\\r\\n", "\n"));
                    String fileName = "backend_cert.pfx";
                    writeBytesToFile(data, localFolder + "/" + fileName);
                    parameters.remove("pfx");
                    parameters.put("certFile", fileName);

                } catch (AppException e) {
                    throw new AppException("Can't write certificate to disc", ErrorCode.UNXPECTED_ERROR, e);
                }
            }
        }
    }


    public void writeContent(ObjectMapper mapper, ExportAPI exportAPI, File localFolder, String configFile) throws AppException {
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                mapper.writeValue(System.out, exportAPI);
            } else {
                mapper.writeValue(new File(localFolder.getCanonicalPath() + configFile), exportAPI);
            }
        } catch (Exception e) {
            throw new AppException("Can't create API-Configuration file for API: '" + exportAPI.getName() + "' exposed on path: '" + exportAPI.getPath() + "'.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileDest)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public String getAPIExportFolder(String apiExposurePath) {
        if (apiExposurePath.startsWith("/"))
            apiExposurePath = apiExposurePath.replaceFirst("/", "");
        if (apiExposurePath.endsWith("/"))
            apiExposurePath = apiExposurePath.substring(0, apiExposurePath.length() - 1);
        apiExposurePath = apiExposurePath.replace("/", "-");
        return apiExposurePath;
    }

    public void validateFolder(File localFolder) throws AppException {
        if (localFolder.exists()) {
            if (Boolean.TRUE.equals(params.isDeleteTarget())) {
                LOG.debug("Existing local export folder: {} already exists and will be deleted.", localFolder);
                try {
                    FileUtils.deleteDirectory(localFolder);
                    LOG.debug("Directory Deleted : {}", localFolder);
                } catch (IOException e) {
                    throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
                }
            } else {
                LOG.warn("Local export folder: {} already exists. API will not be exported. (You may set -deleteTarget)", localFolder);
                return;
            }
        }
        if (!localFolder.mkdirs()) {
            throw new AppException("Cant create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
        }
    }
}
