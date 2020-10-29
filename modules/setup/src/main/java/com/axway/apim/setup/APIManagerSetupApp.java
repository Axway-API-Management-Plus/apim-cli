package com.axway.apim.setup;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.setup.adapter.JSONAPIManagerConfigAdapter;
import com.axway.apim.setup.impl.APIManagerSetupResultHandler;
import com.axway.apim.setup.impl.APIManagerSetupResultHandler.ResultHandler;
import com.axway.apim.setup.lib.APIManagerSetupExportCLIOptions;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.lib.APIManagerSetupImportCLIOptions;

public class APIManagerSetupApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIManagerSetupApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "API-Manager - S E T U P";
	}

	@Override
	public String getVersion() {
		return APIManagerSetupApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "setup";
	}

	@Override
	public String getGroupDescription() {
		return "Manage your API-Manager Config/Remote-Hosts & Alerts";
	}
	
	@CLIServiceMethod(name = "get", description = "Get actual API-Manager configuration")
	public static int exportConfig(String args[]) {
		APIManagerSetupExportParams params;
		try {
			params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		/*} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();*/
		}
		APIManagerSetupApp app = new APIManagerSetupApp();
		return app.runExport(params).getRc();
	}
	
	@CLIServiceMethod(name = "import", description = "Import configuration into API-Manager")
	public static int importConfig(String args[]) {
		StandardImportParams params;
		try {
			params = (StandardImportParams) APIManagerSetupImportCLIOptions.create(args).getParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		/*} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();*/
		}
		APIManagerSetupApp managerConfigApp = new APIManagerSetupApp();
		return managerConfigApp.importConfig(params).getRc();
	}

	public ExportResult runExport(APIManagerSetupExportParams params) {
		ExportResult result = new ExportResult();
		try {
			switch(params.getOutputFormat()) {
			case console:
				return exportAPIManagerSetup(params, ResultHandler.CONSOLE_EXPORTER, result);
			case json:
				return exportAPIManagerSetup(params, ResultHandler.JSON_EXPORTER, result);
			default:
				return exportAPIManagerSetup(params, ResultHandler.CONSOLE_EXPORTER, result);
			}
		} catch (AppException e) {
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(e.getMessage(), e);
				result.setRc(new ErrorCodeMapper().getMapedErrorCode(errorState.getErrorCode()).getCode());
				return result;
			} else {
				LOG.error(e.getMessage(), e);
				result.setRc(new ErrorCodeMapper().getMapedErrorCode(e.getErrorCode()).getCode());
				return result;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setRc(ErrorCode.UNXPECTED_ERROR.getCode());
			return result;
		}
	}

	private ExportResult exportAPIManagerSetup(APIManagerSetupExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
		// We need to clean some Singleton-Instances, as tests are running in the same JVM
		APIManagerAdapter.deleteInstance();
		ErrorState.deleteInstance();
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
			LOG.info("");
			LOG.error("Please check the log. At least one error was recorded.");
		} else {
			LOG.info("API-Manager configuration successfully exported.");
		}
		APIManagerAdapter.deleteInstance();
		result.setRc(ErrorState.getInstance().getErrorCode().getCode());
		return result;
	}
	
	public ImportResult importConfig(StandardImportParams params) {
		ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
		ImportResult result = new ImportResult();
		String updatedAssets = "";
		try {			
			// Clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();

			errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
			
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			
			APIManagerConfig desiredConfig = new JSONAPIManagerConfigAdapter(params).getManagerConfig();
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
				Iterator<RemoteHost> it = desiredConfig.getRemoteHosts().values().iterator();
				while(it.hasNext()) {
					RemoteHost desiredRemoteHost = it.next();
					RemoteHost actualRemoteHost = apimAdapter.remoteHostsAdapter.getRemoteHost(desiredRemoteHost.getName(), desiredRemoteHost.getPort());
					apimAdapter.remoteHostsAdapter.createOrUpdateRemoteHost(desiredRemoteHost, actualRemoteHost);
				}
				updatedAssets+="Remote-Hosts";
				LOG.debug("API-Manager remote host(s) successfully updated.");
			}
			LOG.info("API-Manager configuration ("+updatedAssets+") successfully updated.");
			return result;
		} catch (AppException ap) { 
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				result.setRc(errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode());
				return result;
			} else {
				LOG.error(ap.getMessage(), ap);
				result.setRc(errorCodeMapper.getMapedErrorCode(ap.getErrorCode()).getCode());
				return result;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setRc(ErrorCode.UNXPECTED_ERROR.getCode());
			return result;
		}
	}

	public static void main(String args[]) { 
		int rc = exportConfig(args);
		System.exit(rc);
	}


}
