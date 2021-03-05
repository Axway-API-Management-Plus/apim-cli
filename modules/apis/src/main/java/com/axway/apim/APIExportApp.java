package com.axway.apim;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.APIResultHandler;
import com.axway.apim.api.export.impl.APIResultHandler.APIListImpl;
import com.axway.apim.api.export.lib.cli.CLIAPIApproveOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIDeleteOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIGrantAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIUnpublishOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIUpgradeAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIChangeAPIOptions;
import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.api.model.Organization;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;

/**
 * 
 * @author cwiechmann@axway.com
 */
public class APIExportApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(APIExportApp.class);
	
	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	
	private static ErrorState errorState = ErrorState.getInstance();

	public static void main(String args[]) { 
		int rc = grantAccess(args);
		System.exit(rc);
	}
	
	@CLIServiceMethod(name = "get", description = "Get APIs from API-Manager in different formats")
	public static int exportAPI(String args[]) {
		APIExportParams params;
		try {
			params = (APIExportParams) CLIAPIExportOptions.create(args).getParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		}
		APIExportApp apiExportApp = new APIExportApp();
		return apiExportApp.exportAPI(params);
	}

	public int exportAPI(APIExportParams params) {
		try {
			params.validateRequiredParameters();
			deleteInstances();

			switch(params.getOutputFormat()) {
			case console:
				return execute(params, APIListImpl.CONSOLE_EXPORTER);
			case json:
				return execute(params, APIListImpl.JSON_EXPORTER);
			case csv:
				return execute(params, APIListImpl.CSV_EXPORTER);
			default:
				return execute(params, APIListImpl.CONSOLE_EXPORTER);
			}
		} catch (AppException e) {
			
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(e.getMessage(), e);
				return errorCodeMapper.getMapedErrorCode(errorState.getErrorCode()).getCode();
			} else {
				LOG.error(e.getMessage(), e);
				return errorCodeMapper.getMapedErrorCode(e.getErrorCode()).getCode();
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "delete", description = "Delete the selected APIs from the API-Manager")
	public static int delete(String args[]) {
		try {
			deleteInstances();
			APIExportParams params = (APIExportParams) CLIAPIDeleteOptions.create(args).getParams();
			return execute(params, APIListImpl.API_DELETE_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "unpublish", description = "Unpublish the selected APIs")
	public static int unpublish(String args[]) {
		try {
			deleteInstances();
			APIExportParams params = (APIExportParams) CLIAPIUnpublishOptions.create(args).getParams();
			return execute(params, APIListImpl.API_UNPUBLISH_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "publish", description = "Publish the selected APIs")
	public static int publish(String args[]) {
		try {
			deleteInstances();
			APIExportParams params = (APIExportParams) CLIAPIUnpublishOptions.create(args).getParams();
			return execute(params, APIListImpl.API_PUBLISH_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "change", description = "Changes the selected APIs according to given parameters")
	public static int change(String args[]) {
		try {
			deleteInstances();
			APIChangeParams params = (APIChangeParams) CLIChangeAPIOptions.create(args).getParams();
			return execute(params, APIListImpl.API_CHANGE_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "approve", description = "Approves selected APIs that are in pending state")
	public static int approve(String args[]) {
		try {
			deleteInstances();
			
			APIApproveParams params = (APIApproveParams) CLIAPIApproveOptions.create(args).getParams();
			
			return execute(params, APIListImpl.API_APPROVE_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "upgrade-access", description = "Upgrades access for one or more APIs based on a given 'old/reference' API.")
	public static int upgradeAccess(String args[]) {
		try {
			deleteInstances();
			
			APIUpgradeAccessParams params = (APIUpgradeAccessParams) CLIAPIUpgradeAccessOptions.create(args).getParams();
			
			APIExportApp app = new APIExportApp();
			ExportResult result = app.uprgradeAPI(params, APIListImpl.API_UPGRADE_ACCESS_HANDLE);
			return result.getRc();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	@CLIServiceMethod(name = "grant-access", description = "Grant access to selected APIs to the given organization.")
	public static int grantAccess(String args[]) {
		try {
			deleteInstances();
			
			APIGrantAccessParams params = (APIGrantAccessParams) CLIAPIGrantAccessOptions.create(args).getParams();
			
			APIExportApp app = new APIExportApp();
			ExportResult result = app.grantAccessToAPI(params, APIListImpl.API_GRANT_ACCESS_HANDLER);
			return result.getRc();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}
	
	private static int execute(APIExportParams params, APIListImpl resultHandlerImpl) {
		try {

			APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
			
			APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
			APIFilter filter = resultHandler.getFilter();

			List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);
			if(apis.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No APIs found using filter: " + filter);
				} else {
					LOG.info("No APIs found based on the given filters.");
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
			if(ErrorState.getInstance().hasError()) {
				ErrorState.getInstance().logErrorMessages(LOG);
			}
			return ErrorState.getInstance().getErrorCode().getCode();
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
	
	public ExportResult uprgradeAPI(APIUpgradeAccessParams params, APIListImpl resultHandlerImpl) {
		ExportResult result = new ExportResult();
		try {
			params.validateRequiredParameters();
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
			if(!APIManagerAdapter.hasAdminAccount()) {
				LOG.error("Upgrading API-Access needs admin access.");
				result.setRc(ErrorCode.NO_ADMIN_ROLE_USER.getCode());
				return result;
			}
			// Get the reference API from API-Manager
			API referenceAPI = apimanagerAdapter.apiAdapter.getAPI(params.getReferenceAPIFilter(), true);
			if(referenceAPI == null) {
				LOG.info("Published reference API for upgrade access not found using filter: " + params.getReferenceAPIFilter());
				return result;
			}
			params.setReferenceAPI(referenceAPI);
			// Get all APIs to be upgraded
			APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
			APIFilter filter = resultHandler.getFilter();
			List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);

			if(apis.size()==0) {
				LOG.info("No published APIs found using filter: " + filter);
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
	
	public ExportResult grantAccessToAPI(APIGrantAccessParams params, APIListImpl resultHandlerImpl) {
		ExportResult result = new ExportResult();
		try {
			params.validateRequiredParameters();
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
			if(!APIManagerAdapter.hasAdminAccount()) {
				LOG.error("Upgrading API-Access needs admin access.");
				result.setRc(ErrorCode.NO_ADMIN_ROLE_USER.getCode());
				return result;
			}
			// Get all organizations that should be granted
			List<Organization> orgs = apimanagerAdapter.orgAdapter.getOrgs(params.getOrganizationFilter());
			if(orgs == null || orgs.size() == 0) {
				LOG.info("No organization found to grant access to using filter: " + params.getOrganizationFilter());
				return result;
			}
			// Get all APIs that should be granted access
			List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(params.getAPIFilter(), true);
			if(apis == null || apis.size() == 0) {
				LOG.info("No published APIs to grant access to found using filter: " + params.getAPIFilter());
				return result;
			}
			LOG.info(apis.size() + " API(s) and " + orgs.size() + " Organization(s) selected.");
			params.setOrgs(orgs);
			params.setApis(apis);
			APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
			resultHandler.execute(apis);
			if(resultHandler.hasError()) {
				LOG.info("");
				LOG.error("Please check the log. At least one error was recorded.");
			} else {
				LOG.debug("Successfully selected " + apis.size() + " API(s).");
			}
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
	
	private static void deleteInstances() throws AppException {
		// We need to clean some Singleton-Instances, as tests are running in the same JVM
		APIManagerAdapter.deleteInstance();
		ErrorState.deleteInstance();
		APIMHttpClient.deleteInstances();
	}
	
	@Override
	public String getName() {
		return "API - E X P O R T / U T I L S";
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
