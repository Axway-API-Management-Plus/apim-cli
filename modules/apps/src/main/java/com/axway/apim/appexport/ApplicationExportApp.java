package com.axway.apim.appexport;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.ApplicationExporter.ResultHandler;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ApplicationExportApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExportApp.class);

    @Override
    public String getName() {
        return "Application - E X P O R T / U T I L S";
    }

    @Override
    public String getVersion() {
        return ApplicationExportApp.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getGroupId() {
        return "app";
    }

    @Override
    public String getGroupDescription() {
        return "Manage your applications";
    }

    @CLIServiceMethod(name = "get", description = "Get Applications from the API-Manager in different formats")
    public static int export(String[] args) {
        AppExportParams params;
        try {
            params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        return ApplicationExportApp.export(params).getRc();
    }

    @CLIServiceMethod(name = "delete", description = "Delete selected application(s) from the API-Manager")
    public static int delete(String[] args) {
        AppExportParams params;
        try {
            params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        return ApplicationExportApp.delete(params).getRc();
    }

    public static ExportResult delete(AppExportParams params) {
        ExportResult result = new ExportResult();
        try {
            return runExport(params, ResultHandler.DELETE_APP_HANDLER, result);
        } catch (AppException e) {
            e.logException(LOG);
            result.setError(new ErrorCodeMapper().getMapedErrorCode(e.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    public static ExportResult export(AppExportParams params) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            switch (params.getOutputFormat()) {
                case json:
                    return runExport(params, ResultHandler.JSON_EXPORTER, result);
                case yaml:
                    return runExport(params, ResultHandler.YAML_EXPORTER, result);
                case csv:
                    return runExport(params, ResultHandler.CSV_EXPORTER, result);
                default:
                    return runExport(params, ResultHandler.CONSOLE_EXPORTER, result);
            }
        } catch (AppException e) {
            e.logException(LOG);
            result.setError(new ErrorCodeMapper().getMapedErrorCode(e.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    private static ExportResult runExport(AppExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        try {
            ApplicationExporter exporter = ApplicationExporter.create(exportImpl, params, result);
            List<ClientApplication> apps = apimanagerAdapter.getAppAdapter().getApplications(exporter.getFilter(), true);
            if (apps.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("No applications found using filter: {}", exporter.getFilter());
                } else {
                    LOG.info("No applications found based on the given filters.");
                }
            } else {
                LOG.info("Found {} application(s).", apps.size());
                exporter.export(apps);
            }
            return result;
        }finally {
            Utils.deleteInstance(apimanagerAdapter);
        }
    }
}
