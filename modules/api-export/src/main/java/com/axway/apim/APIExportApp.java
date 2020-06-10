package com.axway.apim;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.APIExporter;
import com.axway.apim.api.export.impl.APIExporter.ExportImpl;
import com.axway.apim.api.export.lib.APIExportCLIOptions;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.lib.utils.rest.Transaction;

/**
 * 
 * @author cwiechmann@axway.com
 */
public class APIExportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIExportApp.class);

	public static void main(String args[]) { 
		int rc = export(args);
		System.exit(rc);
	}
	
	@CLIServiceMethod(name = "export", description = "Export APIs from the API-Manager")
	public static int export(String args[]) {
		return runExport(args, ExportImpl.JSON_EXPORTER);
	}
	
	@CLIServiceMethod(name = "list", description = "List APIs from the API-Manager")
	public static int list(String args[]) {
		return runExport(args, ExportImpl.CONSOLE_EXPORTER);
	}
	
	private static int runExport(String[] args, ExportImpl exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();
			Transaction.deleteInstance();

			APIExportParams params = new APIExportParams(new APIExportCLIOptions(args));
			APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
			
			APIExporter exporter = APIExporter.create(exportImpl, params);
			APIFilter filter = exporter.getFilter();

			List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);
			if(apis.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No APIs found using filter: " + filter);
				} else {
					LOG.info("No APIs found based on the given criteria.");
				}
			} else {
				LOG.info("Selected " + apis.size() + " API(s) to export.");
				
				exporter.export(apis);
				if(exporter.hasError()) {
					LOG.info("Please check the log. At least one error was recorded.");
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
		return "API Export";
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
