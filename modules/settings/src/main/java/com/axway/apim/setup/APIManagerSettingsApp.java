package com.axway.apim.setup;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.setup.adapter.APIManagerConfigAdapter;
import com.axway.apim.setup.impl.APIManagerSetupResultHandler;
import com.axway.apim.setup.impl.ResultHandler;
import com.axway.apim.setup.lib.APIManagerSetupExportCLIOptions;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.lib.APIManagerSetupImportCLIOptions;
import com.axway.apim.setup.model.APIManagerConfig;
import com.axway.apim.setup.model.Quotas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIManagerSettingsApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerSettingsApp.class);

    @Override
    public String getName() {
        return "API-Manager - S E T T I N G S";
    }

    @Override
    public String getVersion() {
        return APIManagerSettingsApp.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getGroupId() {
        return "settings";
    }

    @Override
    public String getGroupDescription() {
        return "Manage your API-Manager Config/Remote-Hosts & Alerts";
    }

    @CLIServiceMethod(name = "get", description = "Get actual API-Manager configuration")
    public static int exportConfig(String[] args) {
        APIManagerSetupExportParams params;
        try {
            params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error processing get", e);
            return e.getError().getCode();
        }
        APIManagerSettingsApp app = new APIManagerSettingsApp();
        return app.runExport(params).getRc();
    }

    @CLIServiceMethod(name = "import", description = "Import configuration into API-Manager")
    public static int importConfig(String[] args) {
        StandardImportParams params;
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        try {
            params = (StandardImportParams) APIManagerSetupImportCLIOptions.create(args).getParams();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return errorCodeMapper.getMapedErrorCode(e.getError()).getCode();
        }
        APIManagerSettingsApp managerConfigApp = new APIManagerSettingsApp();
        return errorCodeMapper.getMapedErrorCode(managerConfigApp.importConfig(params).getErrorCode()).getCode();
    }

    public ExportResult runExport(APIManagerSetupExportParams params) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            if (params.getOutputFormat() == StandardExportParams.OutputFormat.json)
                return exportAPIManagerSetup(params, ResultHandler.JSON_EXPORTER, result);
            if (params.getOutputFormat() == StandardExportParams.OutputFormat.yaml)
                return exportAPIManagerSetup(params, ResultHandler.YAML_EXPORTER, result);
            return exportAPIManagerSetup(params, ResultHandler.CONSOLE_EXPORTER, result);
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

    private ExportResult exportAPIManagerSetup(APIManagerSetupExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        try {
            APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(exportImpl, params, result);
            APIManagerConfig apiManagerConfig = new APIManagerConfig();
            if (params.isExportConfig()) {
                apiManagerConfig.setConfig(adapter.getConfigAdapter().getConfig());
            }
            if (params.isExportAlerts()) {
                apiManagerConfig.setAlerts(adapter.getAlertsAdapter().getAlerts());
            }
            if (params.isExportRemoteHosts()) {
                apiManagerConfig.setRemoteHosts(adapter.getRemoteHostsAdapter().getRemoteHosts(exporter.getRemoteHostFilter()));
            }
            if (params.isExportQuotas()) {
                apiManagerConfig.setQuotas(getGlobalQuotas(adapter));
            }
            exporter.export(apiManagerConfig);
            if (exporter.hasError()) {
                LOG.error("Please check the log. At least one error was recorded.");
            } else {
                LOG.info("API-Manager configuration successfully exported.");
            }
            return result;
        } finally {
            Utils.deleteInstance(adapter);
        }
    }

    public Quotas getGlobalQuotas(APIManagerAdapter adapter) throws AppException {
        Quotas quotas = new Quotas();
        APIQuota systemQuota = adapter.getQuotaAdapter().getDefaultQuota(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT);
        APIQuota applicationQuota = adapter.getQuotaAdapter().getDefaultQuota(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT);
        QuotaRestriction systemQuotaRestriction = systemQuota.getRestrictions().stream().filter(
            quotaRestriction -> quotaRestriction.getApi().equals("*")).findFirst().orElse(null);
        QuotaRestriction applicationQuotaRestriction = applicationQuota.getRestrictions().stream().filter(
            quotaRestriction -> quotaRestriction.getApi().equals("*")).findFirst().orElse(null);
        quotas.setSystemQuota(systemQuotaRestriction);
        quotas.setApplicationQuota(applicationQuotaRestriction);
        return quotas;
    }

    public Result importConfig(StandardImportParams params) {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        Result result = new Result();
        StringBuilder updatedAssets = new StringBuilder();
        APIManagerAdapter apimAdapter = null;
        try {
            params.validateRequiredParameters();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
            apimAdapter = APIManagerAdapter.getInstance();
            APIManagerConfig desiredConfig = new APIManagerConfigAdapter(params).getManagerConfig();
            if (desiredConfig.getConfig() != null) {
                apimAdapter.getConfigAdapter().updateConfiguration(desiredConfig.getConfig());
                updatedAssets.append("Config ");
                LOG.debug("API-Manager configuration successfully updated.");
            }
            if (desiredConfig.getAlerts() != null) {
                apimAdapter.getAlertsAdapter().updateAlerts(desiredConfig.getAlerts());
                updatedAssets.append("Alerts ");
                LOG.debug("API-Manager alerts successfully updated.");
            }
            if (desiredConfig.getRemoteHosts() != null) {
                for (RemoteHost desiredRemoteHost : desiredConfig.getRemoteHosts().values()) {
                    RemoteHost actualRemoteHost = apimAdapter.getRemoteHostsAdapter().getRemoteHost(desiredRemoteHost.getName(), desiredRemoteHost.getPort());
                    apimAdapter.getRemoteHostsAdapter().createOrUpdateRemoteHost(desiredRemoteHost, actualRemoteHost);
                }
                updatedAssets.append("Remote-Hosts ");
                LOG.debug("API-Manager remote host(s) successfully updated.");
            }
            if (desiredConfig.getQuotas() != null) {
                updatedAssets.append("GlobalQuotas");
                upsertGlobalSystemQuota(desiredConfig.getQuotas());
                upsertGlobalApplicationQuota(desiredConfig.getQuotas());
                LOG.debug("API-Manager Global Quotas successfully updated.");
            }
            LOG.info("API-Manager configuration {} successfully updated.", updatedAssets);
            return result;
        } catch (AppException ap) {
            ap.logException(LOG);
            result.setError(errorCodeMapper.getMapedErrorCode(ap.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        } finally {
            Utils.deleteInstance(apimAdapter);
        }
    }

    public void upsertGlobalSystemQuota(Quotas quotas) throws AppException {
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        APIManagerQuotaAdapter quotaAdapter = adapter.getQuotaAdapter();
        QuotaRestriction systemQuotaRestriction = quotas.getSystemQuota();
        APIQuota systemQuota = quotaAdapter.getDefaultQuota(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT);
        if (systemQuotaRestriction != null && systemQuotaRestriction.getApi() != null) { // Updating quota
            LOG.debug("Updating System Global Quota : {}", systemQuotaRestriction);
            if (systemQuota.getRestrictions() != null) {
                removeExistingGlobalQuota(systemQuota); // Remove exising quota to update
                systemQuota.getRestrictions().add(systemQuotaRestriction);
            }

        }else { // Removing system quota
            LOG.debug("removing System Global Quota ");
            if (systemQuota.getRestrictions() != null) {
                removeExistingGlobalQuota(systemQuota);
            }
        }
        quotaAdapter.saveQuota(systemQuota, APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT.getQuotaId());
        LOG.debug("System Global Quota is updated");
    }

    public void removeExistingGlobalQuota( APIQuota systemQuota) {
        systemQuota.getRestrictions().removeIf(quotaRestriction -> quotaRestriction.getApi().equals("*"));
    }

    public void upsertGlobalApplicationQuota(Quotas quotas) throws AppException {
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        APIManagerQuotaAdapter quotaAdapter = adapter.getQuotaAdapter();
        QuotaRestriction applicationQuotaRestriction = quotas.getApplicationQuota();
        APIQuota applicationQuota = quotaAdapter.getDefaultQuota(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT);
        if (applicationQuotaRestriction != null && applicationQuotaRestriction.getApi() != null) {
            LOG.debug("Updating Application Global Quota : {}", applicationQuotaRestriction);
            if (applicationQuota.getRestrictions() != null) {
                removeExistingGlobalQuota(applicationQuota);
                applicationQuota.getRestrictions().add(applicationQuotaRestriction);
            }
        }else {
            LOG.debug("removing Application Global Quota ");
            if (applicationQuota.getRestrictions() != null) {
                removeExistingGlobalQuota(applicationQuota);
            }
        }
        quotaAdapter.saveQuota(applicationQuota, APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId());
        LOG.debug("Application Global Quota is updated");
    }
}
