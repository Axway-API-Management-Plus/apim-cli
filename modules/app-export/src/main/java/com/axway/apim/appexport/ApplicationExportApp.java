package com.axway.apim.appexport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIMgrAppsAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.JsonApplicationExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.APIMCLIServiceProvider;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ApplicationExportApp implements APIMCLIServiceProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(ApplicationExportApp.class);
	
	ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

	@Override
	public String getName() {
		return "Export applications";
	}
	
	@Override
	public String getVersion() {
		return ApplicationExportApp.class.getPackage().getImplementationVersion();
	}
	
	@Override
	public String getDescription() {
		return "Export applications from the API-Manager";
	}

	@Override
	public String getId() {
		return "app";
	}

	@Override
	public String getMethod() {
		return "export";
	}

	@Override
	public int execute(String[] args) {
		try {
			new AppExportParams(new AppExportCLIOptions(args));
			List<ClientApplication> apps =  new APIMgrAppsAdapter.Builder()
					.hasState(AppExportParams.getInstance().getAppState())
					.hasName(AppExportParams.getInstance().getAppName())
					.hasOrgId(AppExportParams.getInstance().getOrgName())
					.includeQuotas(true)
					.build().getApplications();
			if(apps.size()==0) {
				LOG.info("No applications selected for export");
			} else {
				LOG.info("Selected " + apps.size() + " for export.");
				ApplicationExporter exporter = new JsonApplicationExporter(apps, AppExportParams.getInstance().getTargetFolder());
				exporter.export();
				if(exporter.hasError()) {
					LOG.info("Please check the log. At least one error was recorded.");
				} else {
					LOG.info("Successfully exported " + apps.size() + " application(s).");
				}
			}
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
		return 0;
	}
	


}
