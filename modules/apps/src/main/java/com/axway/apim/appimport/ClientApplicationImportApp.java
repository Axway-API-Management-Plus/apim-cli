package com.axway.apim.appimport;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.adapter.JSONConfigClientAppAdapter;
import com.axway.apim.appimport.lib.AppImportCLIOptions;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;

public class ClientApplicationImportApp implements APIMCLIServiceProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientApplicationImportApp.class);
	
	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

	@Override
	public String getName() {
		return "Application - I M P O R T";
	}
	
	@Override
	public String getVersion() {
		return ClientApplicationImportApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "app";
	}
	
	@Override
	public String getGroupDescription() {
		return "Manage your applications";
	}
	
	@CLIServiceMethod(name = "import", description = "Import application(s) into the API-Manager")
	public static int importApp(String[] args) {
		AppImportParams params;
		try {
			params = new AppImportCLIOptions(args).getAppImportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		ClientApplicationImportApp app = new ClientApplicationImportApp();
		return app.importApp(params).getRc();
	}

	public ImportResult importApp(AppImportParams params) {
		ImportResult result = new ImportResult();
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter.getInstance();
			// Load the desired state of the application
			ClientAppAdapter desiredAppsAdapter = new JSONConfigClientAppAdapter(params);
			List<ClientApplication> desiredApps = desiredAppsAdapter.getApplications();
			ClientAppImportManager importManager = new ClientAppImportManager(desiredAppsAdapter);
			for(ClientApplication desiredApp : desiredApps) {
				ClientApplication actualApp = APIManagerAdapter.getInstance().appAdapter.getApplication(new ClientAppFilter.Builder()
						.includeCredentials(true)
						.includeImage(true)
						.includeQuotas(true)
						.includeOauthResources(true)
						.hasName(desiredApp.getName())
						.build());
				importManager.setDesiredApp(desiredApp);
				importManager.setActualApp(actualApp);
				importManager.replicate();
			}
			LOG.info("Successfully replicated application into API-Manager");
			result.setRc(errorCodeMapper.getMapedErrorCode(ErrorState.getInstance().getErrorCode()).getCode());
			return result;
		} catch (AppException ap) { 
			ErrorState errorState = ErrorState.getInstance();
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(ap.getMessage(), ap);
				result.setRc(errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode());
			} else {
				LOG.error(ap.getMessage(), ap);
				result.setRc(errorCodeMapper.getMapedErrorCode(ap.getErrorCode()).getCode());
			}
			return result;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setRc(ErrorCode.UNXPECTED_ERROR.getCode());
			return result;
		}
	}
	
	public static void main(String args[]) { 
		int rc = importApp(args);
		System.exit(rc);
	}

}
