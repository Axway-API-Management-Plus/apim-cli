package com.axway.apim.setup.config;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.setup.config.adapter.JSONAPIManagerConfigAdapter;
import com.axway.apim.setup.config.impl.ConfigResultHandler;
import com.axway.apim.setup.config.impl.ConfigResultHandler.ResultHandler;
import com.axway.apim.setup.config.lib.ConfigExportCLIOptions;
import com.axway.apim.setup.config.lib.ConfigExportParams;
import com.axway.apim.setup.config.lib.ConfigImportCLIOptions;

public class APIManagerConfigApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIManagerConfigApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "API-Manager - C O N F I G";
	}

	@Override
	public String getVersion() {
		return APIManagerConfigApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "config";
	}

	@Override
	public String getGroupDescription() {
		return "Manage your configuration";
	}
	
	@CLIServiceMethod(name = "get", description = "Get API-Manager configuration in different formats")
	public static int export(String args[]) {
		try {
			ConfigExportParams params = new ConfigExportCLIOptions(args).getConfigExportParams();
			switch(params.getOutputFormat()) {
			case console:
				return runExport(params, ResultHandler.CONSOLE_EXPORTER);
			case json:
				return runExport(params, ResultHandler.JSON_EXPORTER);
			default:
				return runExport(params, ResultHandler.CONSOLE_EXPORTER);
			}
		} catch (AppException e) {
			
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(e.getMessage(), e);
				return new ErrorCodeMapper().getMapedErrorCode(errorState.getErrorCode()).getCode();
			} else {
				LOG.error(e.getMessage(), e);
				return new ErrorCodeMapper().getMapedErrorCode(e.getErrorCode()).getCode();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "import", description = "Import configuration into API-Manager")
	public static int importAPI(String args[]) {
		StandardImportParams params;
		try {
			params = new ConfigImportCLIOptions(args).getImportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		APIManagerConfigApp managerConfigApp = new APIManagerConfigApp();
		return managerConfigApp.importConfig(params);
	}

	private static int runExport(ConfigExportParams params, ResultHandler exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter adapter = APIManagerAdapter.getInstance();

			ConfigResultHandler exporter = ConfigResultHandler.create(exportImpl, params);
			APIManagerConfig config = adapter.configAdapter.getConfig(APIManagerAdapter.hasAdminAccount());
			exporter.export(config);
			if(exporter.hasError()) {
				LOG.info("");
				LOG.error("Please check the log. At least one error was recorded.");
			} else {
				LOG.info("API-Manager configuration successfully exported.");
			}
			APIManagerAdapter.deleteInstance();
		} catch (AppException ap) { 
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode();
			} else {
				LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(ap.getErrorCode()).getCode();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
		return ErrorState.getInstance().getErrorCode().getCode();
	}
	
	public int importConfig(StandardImportParams params) {
		ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
		try {			
			// Clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();

			errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
			
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			
			APIManagerConfig desiredConfig = new JSONAPIManagerConfigAdapter(params).getManagerConfig();

			APIManagerConfig actualConfig = apimAdapter.configAdapter.getConfig(APIManagerAdapter.hasAdminAccount());
			apimAdapter.configAdapter.updateConfiguration(desiredConfig, actualConfig);
			LOG.info("API-Manager configuration successfully updated.");
			return 0;
		} catch (AppException ap) { 
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode();
			} else {
				LOG.error(ap.getMessage(), ap);
				return errorCodeMapper.getMapedErrorCode(ap.getErrorCode()).getCode();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}

	public static void main(String args[]) { 
		int rc = importAPI(args);
		System.exit(rc);
	}


}
