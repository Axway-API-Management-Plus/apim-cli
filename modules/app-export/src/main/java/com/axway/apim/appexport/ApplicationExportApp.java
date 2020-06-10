package com.axway.apim.appexport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
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
import com.axway.apim.lib.utils.rest.Transaction;

public class ApplicationExportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(ApplicationExportApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

	@Override
	public String getName() {
		return "Export applications";
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
	
	@CLIServiceMethod(name = "export", description = "Export applications from the API-Manager")
	public static int export(String[] args) {
		return runExport(args, ExportImpl.JSON_EXPORTER);
	}
	
	@CLIServiceMethod(name = "list", description = "List applications on the console")
	public static int list(String[] args) {
		return runExport(args, ExportImpl.CONSOLE_EXPORTER);
	}

	private static int runExport(String[] args, ExportImpl exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();
			Transaction.deleteInstance();
			
			new AppExportParams(new AppExportCLIOptions(args));
			ClientAppAdapter appAdapter = ClientAppAdapter.create(APIManagerAdapter.getInstance());
			ClientAppFilter filter = new ClientAppFilter.Builder()
					.hasState(AppExportParams.getInstance().getAppState())
					.hasName(AppExportParams.getInstance().getAppName())
					.hasId(AppExportParams.getInstance().getAppId())
					.hasOrganizationName(AppExportParams.getInstance().getOrgName())
					.includeQuotas(true)
					.includeCredentials(true)
					.includeAPIAccess(true)
					.includeImage(true)
					.build();
			List<ClientApplication> apps = appAdapter.getApplications(filter, true);
			if(apps.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No applications found using filter: " + filter);
				} else {
					LOG.info("No applications found based on the given criteria.");
				}
			} else {
				LOG.info("Found " + apps.size() + " application(s).");
				ApplicationExporter exporter = ApplicationExporter.create(apps, exportImpl, AppExportParams.getInstance());
				exporter.export();
				if(exporter.hasError()) {
					LOG.info("Please check the log. At least one error was recorded.");
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
