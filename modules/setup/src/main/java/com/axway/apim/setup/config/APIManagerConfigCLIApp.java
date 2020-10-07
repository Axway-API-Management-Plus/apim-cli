package com.axway.apim.setup.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.setup.config.impl.ConfigResultHandler;
import com.axway.apim.setup.config.impl.ConfigResultHandler.ResultHandler;

public class APIManagerConfigCLIApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIManagerConfigCLIApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "API-Manager - C O N F I G";
	}

	@Override
	public String getVersion() {
		return APIManagerConfigCLIApp.class.getPackage().getImplementationVersion();
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
				LOG.debug("API-Manager configuration successfully exported.");
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
	


	public static void main(String args[]) { 
		int rc = export(args);
		System.exit(rc);
	}


}
