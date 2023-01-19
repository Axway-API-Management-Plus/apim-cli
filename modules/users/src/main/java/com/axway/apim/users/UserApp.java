package com.axway.apim.users;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.User;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.users.adapter.JSONUserAdapter;
import com.axway.apim.users.adapter.UserAdapter;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.impl.UserResultHandler.ResultHandler;
import com.axway.apim.users.lib.UserImportParams;
import com.axway.apim.users.lib.cli.UserChangePasswordCLIOptions;
import com.axway.apim.users.lib.cli.UserDeleteCLIOptions;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.cli.UserImportCLIOptions;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(UserApp.class);

    static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

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
        UserExportParams params;
        try {
            params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        UserApp app = new UserApp();
        return app.export(params).getRc();
    }

    public ExportResult export(UserExportParams params) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            switch (params.getOutputFormat()) {
                case json:
                    return runExport(params, ResultHandler.JSON_EXPORTER, result);
                case console:
                default:
                    return runExport(params, ResultHandler.CONSOLE_EXPORTER, result);
            }
        } catch (AppException e) {
            e.logException(LOG);
            result.setError(new ErrorCodeMapper().getMapedErrorCode(e.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    private ExportResult runExport(UserExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter.deleteInstance();
        APIMHttpClient.deleteInstances();
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        UserResultHandler exporter = UserResultHandler.create(exportImpl, params, result);
        List<User> users = adapter.userAdapter.getUsers(exporter.getFilter());
        if (users.size() == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.info("No users found using filter: {}", exporter.getFilter());
            } else {
                LOG.info("No users found based on the given criteria.");
            }
        } else {
            LOG.info("Found {} user(s).", users.size());
            exporter.export(users);
            if (exporter.hasError()) {
                LOG.info("");
                LOG.error("Please check the log. At least one error was recorded.");
            } else {
                LOG.debug("Successfully exported {} user(s).", users.size());
            }
            APIManagerAdapter.deleteInstance();
        }
        return result;
    }

    @CLIServiceMethod(name = "import", description = "Import user(s) into the API-Manager")
    public static int importUsers(String[] args) {
        UserImportParams params;
        try {
            params = (UserImportParams) UserImportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        UserApp app = new UserApp();
        return app.importUsers(params).getRc();
    }

    public ImportResult importUsers(UserImportParams params) {
        ImportResult result = new ImportResult();
        try {
            params.validateRequiredParameters();
            // We need to clean some Singleton-Instances, as tests are running in the same JVM
            APIManagerAdapter.deleteInstance();
            APIMHttpClient.deleteInstances();
            APIManagerAdapter.getInstance();
            // Load the desired state of the organization
            UserAdapter userAdapter = new JSONUserAdapter(params);
            List<User> desiredUsers = userAdapter.getUsers();
            UserImportManager importManager = new UserImportManager();

            for (User desiredUser : desiredUsers) {
                User actualUser = APIManagerAdapter.getInstance().userAdapter.getUser(
                        new UserFilter.Builder()
                                .hasLoginName(desiredUser.getLoginName())
                                .includeImage(true)
                                .includeCustomProperties(APIManagerAdapter.getInstance().customPropertiesAdapter.getCustomPropertyNames(Type.user))
                                .build());
                User actualUserWithEmail = APIManagerAdapter.getInstance().userAdapter.getUser(new UserFilter.Builder().hasEmail(desiredUser.getEmail()).build());
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
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        } finally {
            try {
                APIManagerAdapter.deleteInstance();
            } catch (AppException e) {
                LOG.error("Error deleting instance", e);
            }
        }
    }

    @CLIServiceMethod(name = "delete", description = "Delete selected user(s) from the API-Manager")
    public static int delete(String[] args) {
        UserExportParams params;
        try {
            params = (UserExportParams) UserDeleteCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        UserApp app = new UserApp();
        return app.delete(params).getRc();
    }

    public ExportResult delete(UserExportParams params) {
        ExportResult result = new ExportResult();
        try {
            return runExport(params, ResultHandler.USER_DELETE_HANDLER, result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    @CLIServiceMethod(name = "changepassword", description = "Changes the password of the selected users.")
    public static int changePassword(String[] args) {
        UserChangePasswordParams params;
        try {
            params = (UserChangePasswordParams) UserChangePasswordCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        UserApp app = new UserApp();
        return app.changePassword(params).getRc();
    }

    public ExportResult changePassword(UserExportParams params) {
        ExportResult result = new ExportResult();
        try {
            return runExport(params, ResultHandler.USER_CHANGE_PASSWORD_HANDLER, result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    public static void main(String[] args) {
        int rc = export(args);
        System.exit(rc);
    }


}
