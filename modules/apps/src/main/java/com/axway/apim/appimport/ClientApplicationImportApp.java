package com.axway.apim.appimport;

import com.axway.apim.ClientAppAdapter;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.adapter.ClientAppConfigAdapter;
import com.axway.apim.appimport.lib.AppImportCLIOptions;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ImportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientApplicationImportApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClientApplicationImportApp.class);

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
        ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
        try {
            params = (AppImportParams) AppImportCLIOptions.create(args).getParams();
            errorCodeMapper.setMapConfiguration(params.getReturnCodeMapping());
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return errorCodeMapper.getMapedErrorCode(e.getError()).getCode();
        }
        ClientApplicationImportApp app = new ClientApplicationImportApp();
        ImportResult importResult = app.importApp(params);
        return errorCodeMapper.getMapedErrorCode(importResult.getErrorCode()).getCode();
    }

    public ImportResult importApp(AppImportParams params) {
        ImportResult result = new ImportResult();
        APIManagerAdapter apiManagerAdapter = null;
        try {
            params.validateRequiredParameters();
            // We need to clean some Singleton-Instances, as tests are running in the same JVM
            apiManagerAdapter = APIManagerAdapter.getInstance();
            // Load the desired state of the application
            ClientAppAdapter desiredAppsAdapter = new ClientAppConfigAdapter(params, result);
            List<ClientApplication> desiredApps = desiredAppsAdapter.getApplications();
            ClientAppImportManager importManager = new ClientAppImportManager();
            for (ClientApplication desiredApp : desiredApps) {
                //I'm reading customProps from desiredApp, what if the desiredApp has no customProps and actualApp has many?
                ClientApplication actualApp = apiManagerAdapter.getAppAdapter().getApplication(new ClientAppFilter.Builder()
                    .includeCredentials(true)
                    .includeImage(true)
                    .includeQuotas(true)
                    .includeAppPermissions(true)
                    .includeOauthResources(true)
                    .includeCustomProperties(desiredApp.getCustomPropertiesKeys())
                    .hasName(desiredApp.getName())
                    .build());
                importManager.setDesiredApp(desiredApp);
                importManager.setActualApp(actualApp);
                importManager.replicate();
                LOG.info("Successfully replicated application: {} into API-Manager", desiredApp.getName());
            }
            return result;
        } catch (AppException ap) {
            ap.logException(LOG);
            LOG.error("Error importing application", ap);
            result.setError(ap.getError());
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setError(ErrorCode.UNXPECTED_ERROR);
            return result;
        } finally {
            Utils.deleteInstance(apiManagerAdapter);
        }
    }
}
