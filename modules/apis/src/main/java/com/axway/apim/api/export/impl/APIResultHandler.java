package com.axway.apim.api.export.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIFilter.POLICY_TRANSLATION;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public abstract class APIResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(APIResultHandler.class);
    APIExportParams params;

    protected Result result;


    public enum APIListImpl {
        JSON_EXPORTER(JsonAPIExporter.class),
        YAML_EXPORTER(YamlAPIExporter.class),
        CONSOLE_EXPORTER(ConsoleAPIExporter.class),
        CSV_EXPORTER(CSVAPIExporter.class),
        DAT_EXPORTER(DATAPIExporter.class),
        API_DELETE_HANDLER(DeleteAPIHandler.class),
        API_PUBLISH_HANDLER(PublishAPIHandler.class),
        API_UNPUBLISH_HANDLER(UnPublishAPIHandler.class),
        API_CHANGE_HANDLER(APIChangeHandler.class),
        API_APPROVE_HANDLER(ApproveAPIHandler.class),
        API_UPGRADE_ACCESS_HANDLE(UpgradeAccessAPIHandler.class),
        API_GRANT_ACCESS_HANDLER(GrantAccessAPIHandler.class),
        API_REVOKE_ACCESS_HANDLER(RevokeAccessAPIHandler.class),

        API_CHECK_CERTS_HANDLER(CheckCertificatesAPIHandler.class);

        private final Class<APIResultHandler> implClass;

        @SuppressWarnings({"rawtypes", "unchecked"})
        APIListImpl(Class clazz) {
            this.implClass = clazz;
        }

        public Class<APIResultHandler> getClazz() {
            return implClass;
        }
    }

    protected APIResultHandler(APIExportParams params) {
        this.params = params;
        this.result = new Result();
    }

    protected APIResultHandler(APIExportParams params, Result result) {
        this.params = params;
        this.result = result;
    }

    public static APIResultHandler create(APIListImpl exportImpl, APIExportParams params) throws AppException {
        try {
            Object[] intArgs = new Object[]{params};
            Constructor<APIResultHandler> constructor = exportImpl.getClazz().getConstructor(APIExportParams.class);
            return constructor.newInstance(intArgs);
        } catch (Exception e) {
            throw new AppException("Error initializing API export handler", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public abstract void execute(List<API> apis) throws AppException;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public abstract APIFilter getFilter();

    protected Builder getBaseAPIFilterBuilder() {
        return new Builder(APIType.CUSTOM)
                .hasVHost(params.getVhost())
                .hasApiPath(params.getApiPath())
                .hasPolicyName(params.getPolicy())
                .hasId(params.getId())
                .hasName(params.getName())
                .hasOrganization(params.getOrganization())
                .hasTag(params.getTag())
                .hasState(params.getState())
                .hasBackendBasepath(params.getBackend())
                .hasInboundSecurity(params.getInboundSecurity())
                .hasOutboundAuthentication(params.getOutboundAuthentication())
                .includeCustomProperties(getAPICustomProperties())
                .translateMethods(METHOD_TRANSLATION.AS_NAME)
                .translatePolicies(POLICY_TRANSLATION.TO_NAME)
                .useFEAPIDefinition(params.isUseFEAPIDefinition())
                .isCreatedOnAfter(params.getCreatedOnAfter())
                .isCreatedOnBefore(params.getCreatedOnBefore())
                .failOnError(false);
    }

    protected List<String> getAPICustomProperties() {
        try {
            return APIManagerAdapter.getInstance().getCustomPropertiesAdapter().getCustomPropertyNames(Type.api);
        } catch (AppException e) {
            LOG.error("Error reading custom properties configuration from API-Manager");
            return Collections.emptyList();
        }
    }

    protected static String getBackendPath(API api) {
        ExportAPI exportAPI = new ExportAPI(api);
        return exportAPI.getBackendBasepath();
    }

    protected static String getUsedSecurity(API api) {
        List<String> usedSecurity = new ArrayList<>();
        Map<String, SecurityProfile> secProfilesMappedByName = new HashMap<>();
        try {
            for (SecurityProfile secProfile : api.getSecurityProfiles()) {
                secProfilesMappedByName.put(secProfile.getName(), secProfile);
            }
            Iterator<InboundProfile> it;
            it = api.getInboundProfiles().values().iterator();
            while (it.hasNext()) {
                InboundProfile profile = it.next();
                SecurityProfile usedSecProfile = secProfilesMappedByName.get(profile.getSecurityProfile());
                // If Security-Profile null only happens for method overrides, then they are using the API-Default --> Skip this InboundProfile
                if (usedSecProfile == null) continue;
                for (SecurityDevice device : usedSecProfile.getDevices()) {
                    if (device.getType() == DeviceType.authPolicy) {
                        String authenticationPolicy = device.getProperties().get("authenticationPolicy");
                        usedSecurity.add(Utils.getExternalPolicyName(authenticationPolicy));
                    } else {
                        usedSecurity.add(device.getType().getName());
                    }
                }
            }
            return usedSecurity.toString().replace("[", "").replace("]", "");
        } catch (AppException e) {
            LOG.error("Error getting security information for API", e);
            return "Err";
        }
    }

    protected static List<String> getUsedPolicies(API api, PolicyType type) {
        return getUsedPolicies(api).get(type);
    }

    protected static Map<PolicyType, List<String>> getUsedPolicies(API api) {
        Iterator<OutboundProfile> it;
        Map<PolicyType, List<String>> result = new EnumMap<>(PolicyType.class);
        List<String> requestPolicies = new ArrayList<>();
        List<String> routingPolicies = new ArrayList<>();
        List<String> responsePolicies = new ArrayList<>();
        List<String> faultHandlerPolicies = new ArrayList<>();
        it = api.getOutboundProfiles().values().iterator();

        while (it.hasNext()) {
            OutboundProfile profile = it.next();
            if (profile.getRequestPolicy() != null && profile.getRequestPolicy().getName() != null) {
                requestPolicies.add(profile.getRequestPolicy().getName());
            }
            if (profile.getRouteType().equals("policy") && profile.getRoutePolicy() != null && profile.getRoutePolicy().getName() != null) {
                routingPolicies.add(profile.getRoutePolicy().getName());
            }
            if (profile.getResponsePolicy() != null && profile.getResponsePolicy().getName() != null) {
                responsePolicies.add(profile.getResponsePolicy().getName());
            }
            if (profile.getFaultHandlerPolicy() != null && profile.getFaultHandlerPolicy().getName() != null) {
                faultHandlerPolicies.add(profile.getFaultHandlerPolicy().getName());
            }
        }
        result.put(PolicyType.REQUEST, requestPolicies);
        result.put(PolicyType.ROUTING, routingPolicies);
        result.put(PolicyType.RESPONSE, responsePolicies);
        result.put(PolicyType.FAULT_HANDLER, faultHandlerPolicies);
        return result;
    }

    protected static String getCustomProps(API api) {
        if (api.getCustomProperties() == null) return "N/A";
        Iterator<String> it = api.getCustomProperties().keySet().iterator();
        List<String> props = new ArrayList<>();
        while (it.hasNext()) {
            String property = it.next();
            String value = api.getCustomProperties().get(property);
            props.add(property + ": " + value);
        }
        return props.toString().replace("[", "").replace("]", "");
    }

    protected static String getTags(API api) {
        if (api.getTags() == null) return "None";
        Iterator<String> it = api.getTags().keySet().iterator();
        List<String> tags = new ArrayList<>();
        while (it.hasNext()) {
            String tagGroup = it.next();
            String[] tagValues = api.getTags().get(tagGroup);
            tags.add(tagGroup + ": " + Arrays.toString(tagValues));
        }
        return tags.toString().replace("[", "").replace("]", "");
    }

    protected static List<String> getGrantedOrganizations(API api) {
        List<String> grantedOrgs = new ArrayList<>();
        try {
            if (api.getClientOrganizations() == null) return grantedOrgs;
            for (Organization org : api.getClientOrganizations()) {
                grantedOrgs.add(org.getName());
            }
            return grantedOrgs;
        } catch (Exception e) {
            LOG.error("Error getting API client organization", e);
            return grantedOrgs;
        }
    }

    protected void validateFolder(File localFolder) throws AppException {
        if (localFolder.exists()) {
            if (Boolean.TRUE.equals(params.isDeleteTarget())) {
                LOG.debug("Existing local export folder: {} already exists and will be deleted.", localFolder);
                try {
                    FileUtils.deleteDirectory(localFolder);
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

    protected String getAPIExportFolder(String apiExposurePath) {
        if (apiExposurePath.startsWith("/"))
            apiExposurePath = apiExposurePath.replaceFirst("/", "");
        if (apiExposurePath.endsWith("/"))
            apiExposurePath = apiExposurePath.substring(0, apiExposurePath.length() - 1);
        apiExposurePath = apiExposurePath.replace("/", "-");
        return apiExposurePath;
    }

    protected void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileDest)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    protected APIFilter createFilter(){
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
