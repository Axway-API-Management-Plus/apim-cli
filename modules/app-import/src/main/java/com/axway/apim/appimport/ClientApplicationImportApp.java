package com.axway.apim.appimport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.appimport.lib.AppImportCLIOptions;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ClientApplicationImportApp implements APIMCLIServiceProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientApplicationImportApp.class);
	
	ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

	@Override
	public String getName() {
		return "Import applications";
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

	@CLIServiceMethod(name = "import", description = "Import an applications into the API-Manager")
	public int importApp(String[] args) {
		try {
			AppImportParams params = new AppImportParams(new AppImportCLIOptions(args));
			APIManagerAdapter.getInstance();
			// Load the desired state of the application
			ClientAppImportManager importManager = new ClientAppImportManager();
			ClientAppAdapter desiredAppsAdapter = new ClientAppAdapter.Builder(params.getValue("config"))
					.build();
			List<ClientApplication> desiredApps = desiredAppsAdapter.getApplications();
			ClientAppAdapter apimClientAppAdapter =  new ClientAppAdapter.Builder(APIManagerAdapter.getInstance())
					.includeQuotas(true)
					.build();
			
			for(ClientApplication desiredApp : desiredApps) {
				ClientApplication actualApp = apimClientAppAdapter.getApplication(desiredApp.getName());
				importManager.setDesiredApp(desiredApp);
				importManager.setActualApp(actualApp);
				importManager.replicate();
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
