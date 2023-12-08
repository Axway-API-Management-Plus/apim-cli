package com.axway.apim.organization;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.adapter.OrgAdapter;
import com.axway.apim.organization.adapter.OrgConfigAdapter;
import com.axway.apim.organization.impl.OrgResultHandler;
import com.axway.apim.organization.impl.OrgResultHandler.ResultHandler;
import com.axway.apim.organization.lib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrganizationApp implements APIMCLIServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationApp.class);

    static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();

    @Override
    public String getName() {
        return "Organization - E X P O R T / U T I L S";
    }

    @Override
    public String getVersion() {
        return OrganizationApp.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getGroupId() {
        return "org";
    }

    @Override
    public String getGroupDescription() {
        return "Manage your organizations";
    }

    @CLIServiceMethod(name = "get", description = "Get Organizations from API-Manager in different formats")
    public static int exportOrgs(String[] args) {
        OrgExportParams params;
        try {
            params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        return exportOrgs(params).getRc();
    }

    public static ExportResult exportOrgs(OrgExportParams params) {
        ExportResult result = new ExportResult();
        try {
            params.validateRequiredParameters();
            switch (params.getOutputFormat()) {
                case json:
                    return exportOrgs(params, ResultHandler.JSON_EXPORTER, result);
                case yaml:
                    return exportOrgs(params, ResultHandler.YAML_EXPORTER, result);
                case console:
                default:
                    return exportOrgs(params, ResultHandler.CONSOLE_EXPORTER, result);
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

    private static ExportResult exportOrgs(OrgExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
        // We need to clean some Singleton-Instances, as tests are running in the same JVM
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        try {
            OrgResultHandler exporter = OrgResultHandler.create(exportImpl, params, result);
            List<Organization> orgs = adapter.getOrgAdapter().getOrgs(exporter.getFilter());
            if (orgs.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("No organizations found using filter: {}", exporter.getFilter());
                } else {
                    LOG.info("No organizations found based on the given criteria.");
                }
            } else {
                LOG.info("Found {} organization(s).", orgs.size());

                exporter.export(orgs);
                if (exporter.hasError()) {
                    LOG.info("");
                    LOG.error("Please check the log. At least one error was recorded.");
                } else {
                    LOG.debug("Successfully exported {} organization(s).", orgs.size());
                }
            }
            return result;
        } finally {
            Utils.deleteInstance(adapter);
        }

    }

    @CLIServiceMethod(name = "import", description = "Import organization(s) into the API-Manager")
    public static int importOrganization(String[] args) {
        OrgImportParams params;
        try {
            params = (OrgImportParams) OrgImportCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error {}", e.getMessage());
            return e.getError().getCode();
        }
        return importOrganization(params);
    }

    public static int importOrganization(OrgImportParams params) {
        APIManagerAdapter apiManagerAdapter = null;
        try {
            params.validateRequiredParameters();
            apiManagerAdapter = APIManagerAdapter.getInstance();
            // Load the desired state of the organization
            OrgAdapter orgAdapter = new OrgConfigAdapter(params);
            List<Organization> desiredOrgs = orgAdapter.getOrganizations();
            OrganizationImportManager importManager = new OrganizationImportManager();
            for (Organization desiredOrg : desiredOrgs) {
                Organization actualOrg = apiManagerAdapter.getOrgAdapter().getOrg(new OrgFilter.Builder()
                    .hasName(desiredOrg.getName())
                    .includeAPIAccess(true)
                    .build());
                importManager.replicate(desiredOrg, actualOrg);
            }
            LOG.info("Successfully replicated organization(s) into API-Manager");
            return ErrorCode.SUCCESS.getCode();
        } catch (AppException ap) {
            ap.logException(LOG);
            return errorCodeMapper.getMapedErrorCode(ap.getError()).getCode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorCode.UNXPECTED_ERROR.getCode();
        } finally {
            Utils.deleteInstance(apiManagerAdapter);
        }
    }

    @CLIServiceMethod(name = "delete", description = "Delete selected organization(s) from the API-Manager")
    public static int delete(String[] args) {
        try {
            OrgExportParams params = (OrgExportParams) OrgDeleteCLIOptions.create(args).getParams();
            return delete(params).getRc();
        } catch (AppException e) {
            LOG.error("Error : {}", e.getMessage());
            return e.getError().getCode();
        }
    }

    public static ExportResult delete(OrgExportParams params) {
        ExportResult result = new ExportResult();
        try {
            return exportOrgs(params, ResultHandler.ORG_DELETE_HANDLER, result);
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
}
