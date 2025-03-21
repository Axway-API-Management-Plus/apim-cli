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
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

    public int importAPI(APIImportParams params) {
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        APIManagerAdapter apimAdapter = null;
        try {
            params.validateRequiredParameters();
            // Clean some Singleton-Instances, as tests are running in the same JVM
            RollbackHandler.deleteInstance();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
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
            APIChangeState changes = new APIChangeState(actualAPI, desiredAPI, params);
            new APIImportManager().applyChanges(changes, params.isForceUpdate(), params.isUpdateOnly());
            APIPropertiesExport.getInstance().store();
            return 0;
        } catch (AppException ap) {
            APIPropertiesExport.getInstance().store(); // Try to create it, even
            if (!ap.getError().equals(ErrorCode.NO_CHANGE)) {
                RollbackHandler rollback = RollbackHandler.getInstance();
                rollback.executeRollback();
            }
            ap.logException(LOG);
            return errorCodeMapper.getMapedErrorCode(ap.getError()).getCode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        } finally {
            Utils.deleteInstance(apimAdapter);
        }
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
