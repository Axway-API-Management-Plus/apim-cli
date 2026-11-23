package com.axway.apim;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.apiimport.lib.cli.CLIAPIImportDatOptions;
import com.axway.apim.apiimport.lib.cli.CLIAPIImportOptions;
import com.axway.apim.apiimport.lib.params.APIImportDatParams;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.apiimport.rollback.RollbackHandler;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.error.ErrorDetails;
import com.axway.apim.lib.utils.Utils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the Entry-Point of program and responsible to:
 * - read the command-line parameters to create a <code>CommandParameters</code>
 * - next is to read the API-Contract by creating an <code>APIImportConfig</code> instance and calling getImportAPIDefinition()
 * - the <code>APIManagerAdapter</code> method: <code>getAPIManagerAPI()</code> is used to create the API-Manager API state
 * - An <code>APIChangeState</code> is created based on ImportAPI and API-Manager API
 * - Finally the APIManagerAdapter:applyChanges() is called to replicate the state into the APIManager.
 *
 * @author cwiechmann@axway.com
 */
public class APIImportApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(APIImportApp.class);

    private boolean printErrorDetails = EnvironmentProperties.PRINT_ERROR_DETAILS;

    @CLIServiceMethod(name = "import", description = "Import APIs into the API-Manager")
    public static int importAPI(String[] args) {
        APIImportParams params;
        try {
            params = (APIImportParams) CLIAPIImportOptions.create(args).getParams();
        } catch (AppException e) {
            e.logException(LOG);
            return e.getError().getCode();
        }
        return new APIImportApp().importAPI(params);
    }

    @CLIServiceMethod(name = "import-dat", description = "Import API collection using.dat file.")
    public int importDat(String[] args) {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        APIManagerAdapter apimAdapter = null;
        try {
            APIImportDatParams  params = (APIImportDatParams) CLIAPIImportDatOptions.create(args).getParams();
            params.validateRequiredParameters();
            String orgName = params.getOrgName();
            apimAdapter = APIManagerAdapter.getInstance();
            APIManagerOrganizationAdapter organizationAdapter = apimAdapter.getOrgAdapter();
            Organization organization = organizationAdapter.getOrgForName(orgName);
            if (organization == null) {
                throw new AppException("Invalid orgName " + orgName, ErrorCode.UNKNOWN_ORGANIZATION);
            }
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
            APIManagerAPIAdapter apimApiAdapter = apimAdapter.getApiAdapter();
            String apiDatFilePath = params.getApiDefinition();
            apimApiAdapter.importAPIDatFile(new File(apiDatFilePath),   params.getDatPassword(), organization.getId());
            return 0;
        } catch (AppException ap) {
            return errorCodeMapper.getMapedErrorCode(ap.getError()).getCode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        } finally {
            Utils.deleteInstance(apimAdapter);
        }
    }

    /**
     * Imports the API like {@link #importAPI(APIImportParams)}, but returns the details of an error (issue #594).
     *
     * @param params the import parameters
     * @return "0" if the API was imported successfully (or the return code of an error, which is not an AppException). Otherwise, a
     * one-line JSON summary of the error: error code, error name, mapped error code, message and details. See {@link ErrorDetails}
     */
    public String importAPIwithDetails(APIImportParams params) {
        try {
            return String.valueOf(importAPIwithParams(params));
        } catch (AppException ap) {
            ap.logException(LOG);
            return ErrorDetails.toJson(ap, getErrorCodeMapper(params).getMapedErrorCode(ap.getError()));
        }
    }

    public int importAPI(APIImportParams params) {
        try {
            return importAPIwithParams(params);
        } catch (AppException ap) {
            ap.logException(LOG);
            ErrorCode mappedErrorCode = getErrorCodeMapper(params).getMapedErrorCode(ap.getError());
            if (printErrorDetails) {
                System.err.println(ErrorDetails.toJson(ap, mappedErrorCode));
            }
            return mappedErrorCode.getCode();
        }
    }

    void setPrintErrorDetails(boolean printErrorDetails) {
        this.printErrorDetails = printErrorDetails;
    }

    private ErrorCodeMapper getErrorCodeMapper(APIImportParams params) {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        try {
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
        } catch (RuntimeException e) {
            LOG.error("Invalid returnCodeMapping: {} The error code is not mapped.", params.getReturnCodeMapping(), e);
        }
        return errorCodeMapper;
    }

    /**
     * Imports the API. The rollback and the clean-up is done in this method, hence an AppException is only given to the caller to
     * decide how to report it. The return code mapping must be applied by the caller using the returnCodeMapping of the params.
     *
     * @param params the import parameters
     * @return the return code, if no AppException is thrown
     * @throws AppException if the import fails
     */
    public int importAPIwithParams(APIImportParams params) throws AppException {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        APIManagerAdapter apimAdapter = null;
        try {
            params.validateRequiredParameters();
            // Clean some Singleton-Instances, as tests are running in the same JVM
            RollbackHandler.deleteInstance();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping()); // Fails early if the mapping is invalid
            apimAdapter = APIManagerAdapter.getInstance();
            APIImportConfigAdapter configAdapter = new APIImportConfigAdapter(params);
            // Creates an API-Representation of the desired API
            API desiredAPI = configAdapter.getDesiredAPI();
            List<NameValuePair> filters = new ArrayList<>();
            // If we don't have an AdminAccount available, we ignore published APIs - For OrgAdmins
            // the unpublished or pending APIs become the actual API
            boolean isAdminAccount = APIManagerAdapter.getInstance().hasAdminAccount();
            boolean orgAdminSelfService = APIManagerAdapter.getInstance().getConfigAdapter().getConfig().getOadminSelfServiceEnabled();
            if (!isAdminAccount && !orgAdminSelfService) {
                filters.add(new BasicNameValuePair("field", "state"));
                filters.add(new BasicNameValuePair("op", "ne"));
                filters.add(new BasicNameValuePair("value", "published"));
            }
            // Lookup existing APIs - If found the actualAPI is valid - desiredAPI is used to control what needs to be loaded
            String vHostsMsg = desiredAPI.getVhost() != null ? ", V-Host: " + desiredAPI.getVhost() : "";
            String routingKeyMsg = desiredAPI.getApiRoutingKey() != null ? ", Query-String version: " + desiredAPI.getApiRoutingKey() : "";
            LOG.info("Lookup actual API based on Path: {} {} {}", desiredAPI.getPath(), vHostsMsg, routingKeyMsg);
            APIFilter filter = new APIFilter.Builder(Builder.APIType.ACTUAL_API)
                .hasApiPath(desiredAPI.getPath())
                .hasVHost(desiredAPI.getVhost())
                .hasVersion(desiredAPI.getVersion())
                .includeCustomProperties(desiredAPI.getCustomProperties())
                .hasQueryStringVersion(desiredAPI.getApiRoutingKey())
                .includeClientOrganizations(true) // We have to load clientOrganization, in case they have to be taken over
                .includeQuotas(true) // Quotas must be loaded even if not given, as they have been configured manually
                .includeClientApplications(true) // Client-Apps must be loaded in all cases
                .includeMethods(true)
                .useFilter(filters)
                .useFEAPIDefinition(params.isUseFEAPIDefinition()) // Should API-Definition load from the FE-API?
                .build();
            API actualAPI = apimAdapter.getApiAdapter().getAPI(filter, true);
            validateSafeUpdate(actualAPI, desiredAPI, params);
            APIChangeState changes = new APIChangeState(actualAPI, desiredAPI, params);
            new APIImportManager().applyChanges(changes, params.isForceUpdate(), params.isUpdateOnly());
            APIPropertiesExport.getInstance().store();
            return 0;
        } catch (AppException ap) {
            APIPropertiesExport.getInstance().store(); // Try to create it, even
            if (!ap.getError().equals(ErrorCode.NO_CHANGE)) {
                // Must be done before the API-Manager adapter is cleaned up in finally
                RollbackHandler rollback = RollbackHandler.getInstance();
                rollback.executeRollback();
            }
            throw ap;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        } finally {
            Utils.deleteInstance(apimAdapter);
        }
    }

    void validateSafeUpdate(API actualAPI, API desiredAPI, APIImportParams params) throws AppException {
        if (!params.isSafeUpdate()) return;
        if (actualAPI == null) return;
        if (actualAPI.getName() == null || desiredAPI.getName() == null) return;
        if (actualAPI.getName().equals(desiredAPI.getName())) return;
        if (desiredAPI.getId() != null && desiredAPI.getId().equals(actualAPI.getId())) return;

        throw new AppException(
            "Safe update blocked: Path '" + desiredAPI.getPath() + "' is already used by API '" + actualAPI.getName() +
                "' (ID: " + actualAPI.getId() + "). Desired API name is '" + desiredAPI.getName() + "'.",
            ErrorCode.API_PATH_IN_USE_BY_DIFFERENT_API
        );
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
        return APIImportApp.class.getPackage().getImplementationVersion();
    }

    public String getName() {
        return "API - I M P O R T";
    }
}
