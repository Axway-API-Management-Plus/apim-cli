package com.axway.apim.appexport.impl;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter.Builder;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;

public abstract class ApplicationExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExporter.class);

    private final HashMap<String, String> userIdToLogin = new HashMap<>();

    public enum ResultHandler {
        JSON_EXPORTER(JsonApplicationExporter.class),
        YAML_EXPORTER(YamlApplicationExporter.class),

        CONSOLE_EXPORTER(ConsoleAppExporter.class),
        CSV_EXPORTER(CSVAppExporter.class),
        DELETE_APP_HANDLER(DeleteAppHandler.class);

        private final Class<ApplicationExporter> implClass;

        @SuppressWarnings({"rawtypes", "unchecked"})
        ResultHandler(Class clazz) {
            this.implClass = clazz;
        }

        public Class<ApplicationExporter> getClazz() {
            return implClass;
        }
    }

    AppExportParams params;
    ExportResult result;

    boolean hasError = false;

    public static ApplicationExporter create(ResultHandler exportImpl, AppExportParams params, ExportResult result) throws AppException {
        try {
            Object[] intArgs = new Object[]{params, result};
            Constructor<ApplicationExporter> constructor =
                    exportImpl.getClazz().getConstructor(AppExportParams.class, ExportResult.class);
            return constructor.newInstance(intArgs);
        } catch (Exception e) {
            throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    protected ApplicationExporter(AppExportParams params, ExportResult result) {
        this.params = params;
        this.result = result;
    }

    public abstract void export(List<ClientApplication> apps) throws AppException;

    public boolean hasError() {
        return this.hasError;
    }

    public abstract ClientAppFilter getFilter() throws AppException;

    protected Builder getBaseFilterBuilder() throws AppException {
        Builder builder = new ClientAppFilter.Builder()
                .hasState(params.getState())
                .hasName(params.getName())
                .hasId(params.getId())
                .hasCredential(params.getCredential())
                .hasRedirectUrl(params.getRedirectUrl())
                .hasOrganizationName(params.getOrgName())
                .hasCreatedByLoginName(params.getCreatedBy())
                .includeCustomProperties(getCustomProperties())
                .includeAppPermissions(false)
                .includeOauthResources(false)
                .hasApiName(params.getApiName())
                .includeImage(false);
        if (params.getCredential() != null || params.getRedirectUrl() != null) builder.includeCredentials(true);
        if (params.getApiName() != null) builder.includeAPIAccess(true);
        return builder;
    }

    protected List<String> getCustomProperties() {
        try {
            return APIManagerAdapter.getInstance().customPropertiesAdapter.getCustomPropertyNames(Type.application);
        } catch (AppException e) {
            LOG.error("Error reading custom properties configuration for applications from API-Manager");
            return Collections.emptyList();
        }
    }

    protected String getCreatedBy(String userId, ClientApplication app) {
        if (this.userIdToLogin.containsKey(userId)) return this.userIdToLogin.get(userId);
        String loginName;
        if (userId == null) {
            LOG.error("Application: {} has no createdBy information.", app);
        }
        try {
            loginName = APIManagerAdapter.getInstance().userAdapter.getUserForId(app.getCreatedBy()).getLoginName();
        } catch (AppException e) {
            LOG.error("Error getting createdBy user with Id: {} for application: {}", app.getCreatedBy(), app);
            loginName = app.getCreatedBy();
        }
        this.userIdToLogin.put(userId, loginName);
        return loginName;
    }

    protected Date getCreatedOn(Long createdOn) {
        return new Date(createdOn);
    }
}
