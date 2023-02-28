package com.axway.apim.setup;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.setup.adapter.APIManagerConfigAdapter;
import com.axway.apim.setup.impl.APIManagerSetupResultHandler;
import com.axway.apim.setup.impl.APIManagerSetupResultHandler.ResultHandler;
import com.axway.apim.setup.lib.APIManagerSetupExportCLIOptions;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.lib.APIManagerSetupImportCLIOptions;
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
			LOG.error("Error {}", e.getMessage());
			return e.getError().getCode();
		}
		APIManagerSettingsApp app = new APIManagerSettingsApp();
		return app.runExport(params).getRc();
	}
	
	@CLIServiceMethod(name = "import", description = "Import configuration into API-Manager")
	public static int importConfig(String[] args) {
		StandardImportParams params;
		try {
			params = (StandardImportParams) APIManagerSetupImportCLIOptions.create(args).getParams();
		} catch (AppException e) {
			LOG.error("Error {}" , e.getMessage());
			return e.getError().getCode();
		}
		APIManagerSettingsApp managerConfigApp = new APIManagerSettingsApp();
		return managerConfigApp.importConfig(params).getRc();
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
		APIManagerAdapter.deleteInstance();
		APIMHttpClient.deleteInstances();
		APIManagerAdapter adapter = APIManagerAdapter.getInstance();
		APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(exportImpl, params, result);
		APIManagerConfig apiManagerConfig = new APIManagerConfig();
		if(params.isExportConfig()) {
			apiManagerConfig.setConfig(adapter.configAdapter.getConfig(APIManagerAdapter.hasAdminAccount()));
		}
		if(params.isExportAlerts()) {
			apiManagerConfig.setAlerts(adapter.alertsAdapter.getAlerts());
		}
		if(params.isExportRemoteHosts()) {
			apiManagerConfig.setRemoteHosts(adapter.remoteHostsAdapter.getRemoteHosts(exporter.getRemoteHostFilter()));
		}
		exporter.export(apiManagerConfig);
		if(exporter.hasError()) {
			LOG.error("Please check the log. At least one error was recorded.");
		} else {
			LOG.info("API-Manager configuration successfully exported.");
		}
		APIManagerAdapter.deleteInstance();
		return result;
	}
	
	public ImportResult importConfig(StandardImportParams params) {
		ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
		ImportResult result = new ImportResult();
		String updatedAssets = "";
		try {
			params.validateRequiredParameters();
			// Clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			APIMHttpClient.deleteInstances();
			errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			APIManagerConfig desiredConfig = new APIManagerConfigAdapter(params).getManagerConfig();
			if(desiredConfig.getConfig()!=null) {
				apimAdapter.configAdapter.updateConfiguration(desiredConfig.getConfig());
				updatedAssets+="Config ";
				LOG.debug("API-Manager configuration successfully updated.");
			}
			if(desiredConfig.getAlerts()!=null) {
				apimAdapter.alertsAdapter.updateAlerts(desiredConfig.getAlerts());
				updatedAssets+="Alerts ";
				LOG.debug("API-Manager alerts successfully updated.");
			}

			if(desiredConfig.getRemoteHosts()!=null) {
				for (RemoteHost desiredRemoteHost : desiredConfig.getRemoteHosts().values()) {
					RemoteHost actualRemoteHost = apimAdapter.remoteHostsAdapter.getRemoteHost(desiredRemoteHost.getName(), desiredRemoteHost.getPort());
					apimAdapter.remoteHostsAdapter.createOrUpdateRemoteHost(desiredRemoteHost, actualRemoteHost);
				}
				updatedAssets+="Remote-Hosts";
				LOG.debug("API-Manager remote host(s) successfully updated.");
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
			try {
				APIManagerAdapter.deleteInstance();
			} catch (AppException ignore) { }
		}
	}
}