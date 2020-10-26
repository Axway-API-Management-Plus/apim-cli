package com.axway.apim.users;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.User;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.users.adapter.JSONUserAdapter;
import com.axway.apim.users.adapter.UserAdapter;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.impl.UserResultHandler.ResultHandler;
import com.axway.apim.users.lib.UserDeleteCLIOptions;
import com.axway.apim.users.lib.UserExportCLIOptions;
import com.axway.apim.users.lib.UserExportParams;
import com.axway.apim.users.lib.UserImportCLIOptions;
import com.axway.apim.users.lib.UserImportParams;

public class UserApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(UserApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "User - E X P O R T / U T I L S";
	}

	@Override
	public String getVersion() {
		return UserApp.class.getPackage().getImplementationVersion();
	}

	@Override
	public String getGroupId() {
		return "user";
	}

	@Override
	public String getGroupDescription() {
		return "Manage your users";
	}
	
	@CLIServiceMethod(name = "get", description = "Get users from API-Manager in different formats")
	public static int export(String args[]) {
		UserExportParams params;
		try {
			params = new UserExportCLIOptions(args).getUserExportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		UserApp app = new UserApp();
		return app.export(params).getRc();
	}

	public ExportResult export(UserExportParams params) {
		ExportResult result = new ExportResult();
		try {
			switch(params.getOutputFormat()) {
			case console:
				return runExport(params, ResultHandler.CONSOLE_EXPORTER, result);
			case json:
				return runExport(params, ResultHandler.JSON_EXPORTER, result);
			default:
				return runExport(params, ResultHandler.CONSOLE_EXPORTER, result);
			}
		} catch (AppException e) {
			
			if(errorState.hasError()) {
				errorState.logErrorMessages(LOG);
				if(errorState.isLogStackTrace()) LOG.error(e.getMessage(), e);
				result.setRc(new ErrorCodeMapper().getMapedErrorCode(errorState.getErrorCode()).getCode());
			} else {
				LOG.error(e.getMessage(), e);
				result.setRc(new ErrorCodeMapper().getMapedErrorCode(e.getErrorCode()).getCode());
			}
			return result;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setRc(ErrorCode.UNXPECTED_ERROR.getCode());
			return result;
		}
	}

	private ExportResult runExport(UserExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
		// We need to clean some Singleton-Instances, as tests are running in the same JVM
		APIManagerAdapter.deleteInstance();
		ErrorState.deleteInstance();
		APIMHttpClient.deleteInstances();
		
		APIManagerAdapter adapter = APIManagerAdapter.getInstance();

		UserResultHandler exporter = UserResultHandler.create(exportImpl, params, result);
		List<User> users = adapter.userAdapter.getUsers(exporter.getFilter());
		if(users.size()==0) {
			if(LOG.isDebugEnabled()) {
				LOG.info("No users found using filter: " + exporter.getFilter());
			} else {
				LOG.info("No users found based on the given criteria.");
			}
		} else {
			LOG.info("Found " + users.size() + " user(s).");
			
			exporter.export(users);
			if(exporter.hasError()) {
				LOG.info("");
				LOG.error("Please check the log. At least one error was recorded.");
			} else {
				LOG.debug("Successfully exported " + users.size() + " organization(s).");
			}
		}
		APIManagerAdapter.deleteInstance();
		result.setRc(ErrorState.getInstance().getErrorCode().getCode());
		return result;
	}
	
	@CLIServiceMethod(name = "import", description = "Import user(s) into the API-Manager")
	public static int importUsers(String[] args) {		
		UserImportParams params;
		try {
			params = new UserImportCLIOptions(args).getUserImportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		UserApp app = new UserApp();
		return app.importUsers(params).getRc();
	}
	
	public ImportResult importUsers(UserImportParams params) {
		ImportResult result = new ImportResult();
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter.getInstance();
			// Load the desired state of the organization
			UserAdapter userAdapter = new JSONUserAdapter();
			userAdapter.readConfig(params.getConfig());
			List<User> desiredUsers = userAdapter.getUsers();
			UserImportManager importManager = new UserImportManager();
			for(User desiredUser : desiredUsers) {
				User actualUser = APIManagerAdapter.getInstance().userAdapter.getUser(new UserFilter.Builder().hasLoginName(desiredUser.getLoginName()).includeImage(true).build());
				User actualUserWithEmail = APIManagerAdapter.getInstance().userAdapter.getUser(new UserFilter.Builder().hasEmail(desiredUser.getEmail()).build());
				if(actualUserWithEmail!=null && actualUser!=null && !actualUser.getId().equals(actualUserWithEmail.getId())) {
					LOG.error("A different user: '"+actualUserWithEmail.getLoginName()+"' with the supplied email address: '"+desiredUser.getEmail()+"' already exists. ");
					continue;
				}
				importManager.replicate(desiredUser, actualUser);
			}
			LOG.info("Successfully replicated user(s) into API-Manager");
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
	
	@CLIServiceMethod(name = "delete", description = "Delete selected user(s) from the API-Manager")
	public static int delete(String args[]) {
		UserExportParams params;
		try {
			params = new UserDeleteCLIOptions(args).getUserExportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		UserApp app = new UserApp();
		return app.delete(params).getRc();
	}
	
	public ExportResult delete(UserExportParams params) {
		ExportResult result = new ExportResult();
		try {
			return runExport(params, ResultHandler.ORG_DELETE_HANDLER, result);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setRc(ErrorCode.UNXPECTED_ERROR.getCode());
			return result;
		}
	}

	public static void main(String args[]) { 
		int rc = export(args);
		System.exit(rc);
	}


}
