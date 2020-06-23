package com.axway.apim.appexport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.ApplicationExporter.ExportImpl;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;

public class ApplicationExportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(ApplicationExportApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

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
	public static int export(String args[]) {
		try {
			AppExportParams params = new AppExportParams(new AppExportCLIOptions(args));
			switch(params.getOutputFormat()) {
			case console:
				return runExport(params, ExportImpl.CONSOLE_EXPORTER);
			case json:
				return runExport(params, ExportImpl.JSON_EXPORTER);
			default:
				return runExport(params, ExportImpl.CONSOLE_EXPORTER);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}

	private static int runExport(AppExportParams params, ExportImpl exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();

			APIMgrAppsAdapter appAdapter = new APIMgrAppsAdapter();
			ApplicationExporter exporter = ApplicationExporter.create(exportImpl, params);
			List<ClientApplication> apps = appAdapter.getApplications(exporter.getFilter(), true);
			if(apps.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No applications found using filter: " + exporter.getFilter());
				} else {
					LOG.info("No applications found based on the given criteria.");
				}
			} else {
				LOG.info("Found " + apps.size() + " application(s).");
				
				exporter.export(apps);
				if(exporter.hasError()) {
					LOG.info("");
					LOG.error("Please check the log. At least one error was recorded.");
				} else {
					LOG.debug("Successfully exported " + apps.size() + " application(s).");
				}
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
