package com.axway.apim.setup;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.setup.remotehosts.adapter.JSONRemoteHostsAdapter;
import com.axway.apim.setup.remotehosts.impl.RemoteHostsResultHandler;
import com.axway.apim.setup.remotehosts.impl.RemoteHostsResultHandler.ResultHandler;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportCLIOptions;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsImportCLIOptions;

public class APIManagerRemoteHostApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIManagerRemoteHostApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "API-Manager - C O N F I G";
	}

	@Override
	public String getVersion() {
		return APIManagerRemoteHostApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "remotehost";
	}

	@Override
	public String getGroupDescription() {
		return "Manage your remote hosts";
	}
	
	@CLIServiceMethod(name = "get", description = "Get API-Manager remote hosts in different formats")
	public static int export(String args[]) {
		try {
			RemoteHostsExportParams params = new RemoteHostsExportCLIOptions(args).getParams();
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
	
	@CLIServiceMethod(name = "import", description = "Import remote hosts into API-Manager")
	public static int runImport(String args[]) {
		StandardImportParams params;
		try {
			params = new RemoteHostsImportCLIOptions(args).getImportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		APIManagerRemoteHostApp remoteHostApp = new APIManagerRemoteHostApp();
		return remoteHostApp.importRemoteHosts(params);
	}

	private static int runExport(RemoteHostsExportParams params, ResultHandler exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter adapter = APIManagerAdapter.getInstance();

			RemoteHostsResultHandler exporter = RemoteHostsResultHandler.create(exportImpl, params);
			List<RemoteHost> remoteHosts = adapter.remoteHostsAdapter.getRemoteHosts(exporter.getFilter());
			exporter.export(remoteHosts);
			if(exporter.hasError()) {
				LOG.info("");
				LOG.error("Please check the log. At least one error was recorded.");
			} else {
				LOG.info("API-Manager remote hosts successfully exported.");
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
	
	public int importRemoteHosts(StandardImportParams params) {
		ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
		try {			
			// Clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();

			errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
			
			APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
			
			List<RemoteHost> desiredRemoteHosts = new JSONRemoteHostsAdapter(params).getRemoteHosts();
			
			for(RemoteHost desiredRemoteHost : desiredRemoteHosts) {
				RemoteHost actualRemoteHost = apimAdapter.remoteHostsAdapter.getRemoteHost(desiredRemoteHost.getName(), desiredRemoteHost.getPort());
				apimAdapter.remoteHostsAdapter.createOrUpdateRemoteHost(desiredRemoteHost, actualRemoteHost);				
			}
			LOG.info("API-Manager remote host(s) successfully updated.");
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
		int rc = runImport(args);
		System.exit(rc);
	}


}
