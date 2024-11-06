package com.axway.apim.users;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.User;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.adapter.UserAdapter;
import com.axway.apim.users.adapter.UserConfigAdapter;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.impl.UserResultHandler.ResultHandler;
import com.axway.apim.users.lib.UserImportParams;
import com.axway.apim.users.lib.cli.UserChangePasswordCLIOptions;
import com.axway.apim.users.lib.cli.UserDeleteCLIOptions;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.cli.UserImportCLIOptions;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import com.axway.apim.users.lib.params.UserExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(UserApp.class);
    private static final ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

    @Override
    public String getName() {
        return "User - Management";
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
    public static int export(String[] args) {
        try {
            UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
            UserApp app = new UserApp();
            return app.export(params).getRc();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
    }

    public ExportResult export(UserExportParams params) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            switch (params.getOutputFormat()) {
                case json:
                    return runExport(params, ResultHandler.JSON_EXPORTER, result);
                case yaml:
                    return runExport(params, ResultHandler.YAML_EXPORTER, result);
                case console:
                default:
                    return runExport(params, ResultHandler.CONSOLE_EXPORTER, result);
            }
        } catch (AppException e) {
            e.logException(LOG);
            result.setError(new ErrorCodeMapper().getMapedErrorCode(e.getError()));
            return result;
        } catch (Exception e) {
            LOG.error("Error exporting users ", e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    private ExportResult runExport(UserExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        try {
            UserResultHandler exporter = UserResultHandler.create(exportImpl, params, result);
            List<User> users = adapter.getUserAdapter().getUsers(exporter.getFilter());
            if (users.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No users found using filter: {}", exporter.getFilter());
                } else {
                    LOG.info("No users found based on the given criteria.");
                }
            } else {
                LOG.info("Found {} user(s).", users.size());
                exporter.export(users);
                if (exporter.hasError()) {
                    LOG.error("Please check the log. At least one error was recorded.");
                } else {
                    LOG.debug("Successfully exported {} user(s).", users.size());
                }
            }
            return result;
        } finally {
            Utils.deleteInstance(adapter);
        }
    }

    @CLIServiceMethod(name = "import", description = "Import user(s) into the API-Manager")
    public static int importUsers(String[] args) {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        try {
            UserImportParams params = (UserImportParams) UserImportCLIOptions.create(args).getParams();
            UserApp app = new UserApp();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
            return errorCodeMapper.getMapedErrorCode(app.importUsers(params).getErrorCode()).getCode();
        } catch (AppException e) {
            LOG.error("Error importing user(s): ", e);
            return errorCodeMapper.getMapedErrorCode(e.getError()).getCode();
        }
    }

    public Result importUsers(UserImportParams params) {
        Result result = new Result();
        APIManagerAdapter apiManagerAdapter = null;
        try {
            apiManagerAdapter = APIManagerAdapter.getInstance();
            params.validateRequiredParameters();
            // We need to clean some Singleton-Instances, as tests are running in the same JVM
            // Load the desired state of the organization
            UserAdapter userAdapter = new UserConfigAdapter(params);
            List<User> desiredUsers = userAdapter.getUsers();
            UserImportManager importManager = new UserImportManager();

            for (User desiredUser : desiredUsers) {
                User actualUser = apiManagerAdapter.getUserAdapter().getUser(
                    new UserFilter.Builder()
                        .hasLoginName(desiredUser.getLoginName())
                        .includeImage(true)
                        .includeCustomProperties(apiManagerAdapter.getCustomPropertiesAdapter().getCustomPropertyNames(Type.user))
                        .build());
                User actualUserWithEmail = apiManagerAdapter.getUserAdapter().getUser(new UserFilter.Builder().hasEmail(desiredUser.getEmail()).build());
                if (actualUserWithEmail != null && actualUser != null && !actualUser.getId().equals(actualUserWithEmail.getId())) {
                    LOG.error("A different user: {} with the supplied email address: {} already exists. ", actualUserWithEmail.getLoginName(), desiredUser.getEmail());
                    continue;
                }
                importManager.replicate(desiredUser, actualUser);
            }
            LOG.info("Successfully replicated user(s) into API-Manager");
            return result;
        } catch (AppException ap) {
            ap.logException(LOG);
            result.setError(errorCodeMapper.getMapedErrorCode(ap.getError()));
            return result;
        } catch (Exception e) {
            LOG.error("Error importing users ", e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        } finally {
            Utils.deleteInstance(apiManagerAdapter);
        }
    }

    @CLIServiceMethod(name = "delete", description = "Delete selected user(s) from the API-Manager")
    public static int delete(String[] args) {
        try {
            UserExportParams params = (UserExportParams) UserDeleteCLIOptions.create(args).getParams();
            UserApp app = new UserApp();
            return app.delete(params).getRc();
        } catch (AppException e) {
            LOG.error("Error in deleting user : ", e);
            return e.getError().getCode();
        }
    }

    public ExportResult delete(UserExportParams params) throws AppException {
        ExportResult result = new ExportResult();
        try {
            return runExport(params, ResultHandler.USER_DELETE_HANDLER, result);
        } catch (Exception e) {
            throw new AppException("Error in deleting user", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    @CLIServiceMethod(name = "changepassword", description = "Changes the password of the selected users.")
    public static int changePassword(String[] args) {
        try {
            UserChangePasswordParams params = (UserChangePasswordParams) UserChangePasswordCLIOptions.create(args).getParams();
            UserApp app = new UserApp();
            return app.changePassword(params).getRc();
        } catch (AppException e) {
            LOG.error("Error in change password: ", e);
            return e.getError().getCode();
        }
    }

    public ExportResult changePassword(UserExportParams params) throws AppException {
        ExportResult result = new ExportResult();
        try {
            return runExport(params, ResultHandler.USER_CHANGE_PASSWORD_HANDLER, result);
        } catch (Exception e) {
            throw new AppException("Error in change password", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
