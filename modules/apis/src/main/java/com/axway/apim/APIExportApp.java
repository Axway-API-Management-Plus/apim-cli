package com.axway.apim;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.APIResultHandler;
import com.axway.apim.api.export.impl.APIResultHandler.APIListImpl;
import com.axway.apim.api.export.lib.cli.*;
import com.axway.apim.api.export.lib.params.*;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author cwiechmann@axway.com
 */
public class APIExportApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(APIExportApp.class);
    public static final String CHECK_THE_LOG_AT_LEAST_ONE_ERROR_WAS_RECORDED = "Please check the log. At least one error was recorded.";
    public static final String SUCCESSFULLY_SELECTED_API_S = "Successfully selected {} API(s).";

    static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();


    @CLIServiceMethod(name = "get", description = "Get APIs from API-Manager in different formats")
    public static int exportAPI(String[] args) {
        APIExportParams params;
        try {
            params = (APIExportParams) CLIAPIExportOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error", e);
            return e.getError().getCode();
        }
        APIExportApp apiExportApp = new APIExportApp();
        return apiExportApp.exportAPI(params);
    }

    public int exportAPI(APIExportParams params) {
        try {
            params.validateRequiredParameters();
            deleteInstances();
            switch (params.getOutputFormat()) {
                case json:
                    return execute(params, APIListImpl.JSON_EXPORTER);
                case yaml:
                    return execute(params, APIListImpl.YAML_EXPORTER);
                case csv:
                    return execute(params, APIListImpl.CSV_EXPORTER);
                case dat:
                    return execute(params, APIListImpl.DAT_EXPORTER);
                default:
                    return execute(params, APIListImpl.CONSOLE_EXPORTER);
            }
        } catch (AppException e) {
            e.logException(LOG);
            return errorCodeMapper.getMapedErrorCode(e.getError()).getCode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        }
    }

    @CLIServiceMethod(name = "delete", description = "Delete the selected APIs from the API-Manager")
    public static int delete(String[] args) {
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
    public static int unpublish(String[] args) {
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
    public static int publish(String[] args) {
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
    public static int change(String[] args) {
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
    public static int approve(String[] args) {
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
    public static int upgradeAccess(String[] args) {
        try {
            deleteInstances();
            APIUpgradeAccessParams params = (APIUpgradeAccessParams) CLIAPIUpgradeAccessOptions.create(args).getParams();
            APIExportApp app = new APIExportApp();
            ExportResult result = app.upgradeAPI(params, APIListImpl.API_UPGRADE_ACCESS_HANDLE);
            return result.getRc();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        }
    }

    @CLIServiceMethod(name = "grant-access", description = "Grant access to selected APIs to the given organization.")
    public static int grantAccess(String[] args) {
        try {
            deleteInstances();
            APIGrantAccessParams params = (APIGrantAccessParams) CLIAPIGrantAccessOptions.create(args).getParams();
            APIExportApp app = new APIExportApp();
            ExportResult result = app.grantOrRevokeAccessToAPI(params, APIListImpl.API_GRANT_ACCESS_HANDLER);
            return result.getRc();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        }
    }

    @CLIServiceMethod(name = "revoke-access", description = "Revoke access to an  API to the given organization.")
    public static int revokeAccess(String[] args) {
        try {
            deleteInstances();
            APIGrantAccessParams params = (APIGrantAccessParams) CLIAPIRevokeAccessOptions.create(args).getParams();
            APIExportApp app = new APIExportApp();
            ExportResult result = app.grantOrRevokeAccessToAPI(params, APIListImpl.API_REVOKE_ACCESS_HANDLER);
            return result.getRc();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        }
    }

    @CLIServiceMethod(name = "check-certs", description = "Checks if certificates from selected APIs expire within the specified number of days")
    public static int checkCertificates(String[] args) {
        try {
            deleteInstances();
            APICheckCertificatesParams params = (APICheckCertificatesParams) CLICheckCertificatesOptions.create(args).getParams();
            return execute(params, APIListImpl.API_CHECK_CERTS_HANDLER);
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
            Result result = resultHandler.getResult();
            List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);

            if (apis.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("No APIs found using filter: {}", filter);
                } else {
                    LOG.info("No APIs found based on the given filters.");
                }
            } else {
                LOG.info("{} API(s) selected.", apis.size());
                resultHandler.execute(apis);
                if (resultHandler.getResult().hasError()) {
                    result.setError(resultHandler.getResult().getErrorCode());
                }
                APIManagerAdapter.deleteInstance();
                if (result.hasError()) {
                    if (result.getErrorCode() != ErrorCode.CHECK_CERTS_FOUND_CERTS)
                        LOG.error("An error happened during export. Please check the log");
                }
                return result.getErrorCode().getCode();
            }
            return 0;
        } catch (AppException ap) {
            ap.logException(LOG);
            return ap.getError().getCode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        } finally {
            try {
                // make sure the cache is updated, even an exception is thrown
                APIManagerAdapter.deleteInstance();
            } catch (Exception ignore) {
                LOG.error("Problem in  deleteInstance");
            }
        }
    }

    public ExportResult upgradeAPI(APIUpgradeAccessParams params, APIListImpl resultHandlerImpl) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            deleteInstances();
            APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
            if (!APIManagerAdapter.hasAdminAccount()) {
                LOG.error("Upgrading API-Access needs admin access.");
                result.setError(ErrorCode.NO_ADMIN_ROLE_USER);
                return result;
            }
            // Get the reference API from API-Manager
            API referenceAPI = apimanagerAdapter.apiAdapter.getAPI(params.getReferenceAPIFilter(), true);
            if (referenceAPI == null) {
                LOG.info("Published reference API for upgrade access not found using filter: {}", params.getReferenceAPIFilter());
                return result;
            }
            params.setReferenceAPI(referenceAPI);
            // Get all APIs to be upgraded
            APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
            APIFilter filter = resultHandler.getFilter();
            List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(filter, true);

            if (apis.isEmpty()) {
                LOG.info("No published APIs found using filter: {}", filter);
            } else {
                LOG.info("{} API(s) selected.", apis.size());
                resultHandler.execute(apis);
                if (resultHandler.getResult().hasError()) {
                    LOG.info("");
                    LOG.error(CHECK_THE_LOG_AT_LEAST_ONE_ERROR_WAS_RECORDED);
                } else {
                    LOG.debug(SUCCESSFULLY_SELECTED_API_S, apis.size());
                }
            }
            return result;
        } catch (AppException ap) {
            ap.logException(LOG);
            result.setError(errorCodeMapper.getMapedErrorCode(ap.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    public ExportResult grantOrRevokeAccessToAPI(APIGrantAccessParams params, APIListImpl resultHandlerImpl) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            deleteInstances();
            APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
            if (!APIManagerAdapter.hasAdminAccount()) {
                LOG.error("Upgrading API-Access needs admin access.");
                result.setError(ErrorCode.NO_ADMIN_ROLE_USER);
                return result;
            }
            // Get all organizations that should be granted
            List<Organization> orgs = apimanagerAdapter.orgAdapter.getOrgs(params.getOrganizationFilter());
            if (orgs == null || orgs.isEmpty()) {
                LOG.info("No organization found to grant access to using filter: {}", params.getOrganizationFilter());
                return result;
            }
            // Get all APIs that should be granted access
            List<API> apis = apimanagerAdapter.apiAdapter.getAPIs(params.getAPIFilter(), true);
            if (apis == null || apis.isEmpty()) {
                LOG.info("No published APIs to grant access to found using filter: {}", params.getAPIFilter());
                return result;
            }
            LOG.info("{} API(s) and {} Organization(s) selected.", apis.size(), orgs.size());
            params.setOrgs(orgs);
            params.setApis(apis);

            if (params.getAppId() != null || params.getAppName() != null) {
                LOG.info("Application filter : {}", params.getApplicationFilter());
                ClientApplication application = apimanagerAdapter.appAdapter.getApplication(params.getApplicationFilter());
                if(application == null){
                    throw new AppException("Application not found", ErrorCode.ERR_GRANTING_ACCESS_TO_API);
                }
                params.setClientApplication(application);
            }
            APIResultHandler resultHandler = APIResultHandler.create(resultHandlerImpl, params);
            resultHandler.execute(apis);
            if (resultHandler.getResult().hasError()) {
                result.setError(resultHandler.getResult().getErrorCode());
            }
            return result;
        } catch (AppException ap) {
            ap.logException(LOG);
            result.setError(errorCodeMapper.getMapedErrorCode(ap.getError()));
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        }
    }

    private static void deleteInstances() throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter.deleteInstance();
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
