package com.axway.apim;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.APIResultHandler;
import com.axway.apim.api.export.impl.APIResultHandler.APIListImpl;
import com.axway.apim.api.export.lib.APIExportGetCLIOptions;
import com.axway.apim.api.export.lib.APIExportCLIOptions;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.export.lib.APIDeleteCLIOptions;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;

/**
 * 
 * @author cwiechmann@axway.com
 */
public class APIExportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIExportApp.class);

	public static void main(String args[]) { 
		int rc = delete(args);
		System.exit(rc);
	}
	
	@CLIServiceMethod(name = "get", description = "Get APIs from the API-Manager in different formats")
	public static int export(String args[]) {
		try {
			APIExportParams params = new APIExportParams(new APIExportGetCLIOptions(args));
			switch(params.getExportFormat()) {
			case console:
				return runExport(params, APIListImpl.CONSOLE_EXPORTER);
			case json:
				return runExport(params, APIListImpl.JSON_EXPORTER);
			default:
				return runExport(params, APIListImpl.CONSOLE_EXPORTER);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "delete", description = "Delete the selected APIs from the API-Manager")
	public static int delete(String args[]) {
		try {
			APIExportParams params = new APIExportParams(new APIDeleteCLIOptions(args));
			return runExport(params, APIListImpl.API_DELETE_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "unpublish", description = "Unpublish the selected APIs")
	public static int unpublish(String args[]) {
		try {
			APIExportParams params = new APIExportParams(new APIDeleteCLIOptions(args));
			return runExport(params, APIListImpl.API_UNPUBLISH_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	private static int runExport(APIExportParams params, APIListImpl resultHandlerImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();

			APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
			
			APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
			APIFilter filter = resultHandler.getFilter();

			List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);
			if(apis.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No APIs found using filter: " + filter);
				} else {
					LOG.info("No APIs found based on the given criteria.");
				}
			} else {
				LOG.info(apis.size() + " API(s) selected.");
				resultHandler.execute(apis);
				if(resultHandler.hasError()) {
					LOG.info("");
					LOG.error("Please check the log. At least one error was recorded.");
				} else {
					LOG.debug("Successfully selected " + apis.size() + " API(s).");
				}
			}
			APIManagerAdapter.deleteInstance();
			return 0;
		} catch (AppException ap) {
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				return errorState.getErrorCode().getCode();
			} else {
				LOG.error(ap.getMessage(), ap);
				return ap.getErrorCode().getCode();
			}
		} catch (Exception e) {

			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@Override
	public String getName() {
		return "API - E X P O R T";
	}

	@Override
	public String getGroupId() {
		return "api";
	}
	
	@Override
	public String getGroupDescription() {
		return "Manage your APIs";
	}
	
	@Override
	public String getVersion() {
		return APIExportApp.class.getPackage().getImplementationVersion();
	}
}
