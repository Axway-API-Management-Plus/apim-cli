package com.axway.apim.organization;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.organization.adapter.JSONOrgAdapter;
import com.axway.apim.organization.adapter.OrgAdapter;
import com.axway.apim.organization.impl.OrgResultHandler;
import com.axway.apim.organization.impl.OrgResultHandler.ResultHandler;
import com.axway.apim.organization.lib.OrgDeleteCLIOptions;
import com.axway.apim.organization.lib.OrgExportCLIOptions;
import com.axway.apim.organization.lib.OrgExportParams;
import com.axway.apim.organization.lib.OrgImportCLIOptions;
import com.axway.apim.organization.lib.OrgImportParams;

public class OrganizationCLIApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(OrganizationCLIApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

	@Override
	public String getName() {
		return "Organization - E X P O R T / U T I L S";
	}

	@Override
	public String getVersion() {
		return OrganizationCLIApp.class.getPackage().getImplementationVersion();
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
	public static int export(String args[]) {
		try {
			OrgExportParams params = new OrgExportParams(new OrgExportCLIOptions(args));
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

	private static int runExport(OrgExportParams params, ResultHandler exportImpl) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();
			
			APIManagerAdapter adapter = APIManagerAdapter.getInstance();

			OrgResultHandler exporter = OrgResultHandler.create(exportImpl, params);
			List<Organization> orgs = adapter.orgAdapter.getOrgs(exporter.getFilter());
			if(orgs.size()==0) {
				if(LOG.isDebugEnabled()) {
					LOG.info("No organizations found using filter: " + exporter.getFilter());
				} else {
					LOG.info("No organizations found based on the given criteria.");
				}
			} else {
				LOG.info("Found " + orgs.size() + " organization(s).");
				
				exporter.export(orgs);
				if(exporter.hasError()) {
					LOG.info("");
					LOG.error("Please check the log. At least one error was recorded.");
				} else {
					LOG.debug("Successfully exported " + orgs.size() + " organization(s).");
				}
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
	
	@CLIServiceMethod(name = "import", description = "Import organizatio(s) into the API-Manager")
	public static int importOrganization(String[] args) {
		try {
			// We need to clean some Singleton-Instances, as tests are running in the same JVM
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstance();
			
			OrgImportParams params = new OrgImportParams(new OrgImportCLIOptions(args));
			APIManagerAdapter.getInstance();
			// Load the desired state of the organization
			OrgAdapter orgAdapter = new JSONOrgAdapter();
			orgAdapter.readConfig(params.getValue("config"));
			List<Organization> desiredOrgs = orgAdapter.getOrganizations();
			OrganizationImportManager importManager = new OrganizationImportManager();
			for(Organization desiredOrg : desiredOrgs) {
				Organization actualOrg = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder()
						.hasName(desiredOrg.getName())
						.build());
				importManager.replicate(desiredOrg, actualOrg);
			}
			LOG.info("Successfully replicated organization into API-Manager");
			return errorCodeMapper.getMapedErrorCode(ErrorState.getInstance().getErrorCode()).getCode();
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
	
	@CLIServiceMethod(name = "delete", description = "Delete selected organizatio(s) from the API-Manager")
	public static int delete(String args[]) {
		try {
			OrgExportParams params = new OrgExportParams(new OrgDeleteCLIOptions(args));
			return runExport(params, ResultHandler.ORG_DELETE_HANDLER);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ErrorCode.UNXPECTED_ERROR.getCode();
		}
	}

	public static void main(String args[]) { 
		int rc = export(args);
		System.exit(rc);
	}


}
