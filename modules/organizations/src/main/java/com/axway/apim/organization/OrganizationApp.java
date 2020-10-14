package com.axway.apim.organization;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.lib.ExportResult;
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

public class OrganizationApp implements APIMCLIServiceProvider {

	private static Logger LOG = LoggerFactory.getLogger(OrganizationApp.class);

	static ErrorCodeMapper errorCodeMapper = new ErrorCodeMapper();
	static ErrorState errorState = ErrorState.getInstance();

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
	public static int exportOrgs(String args[]) {
		OrgExportParams params;
		try {
			params = new OrgExportCLIOptions(args).getOrgExportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		OrganizationApp app = new OrganizationApp();
		return app.exportOrgs(params).getRc();
	}
	
	public ExportResult exportOrgs(OrgExportParams params) {
		ExportResult result = new ExportResult();
		try {
			switch(params.getOutputFormat()) {
			case console:
				return exportOrgs(params, ResultHandler.CONSOLE_EXPORTER, result);
			case json:
				return exportOrgs(params, ResultHandler.JSON_EXPORTER, result);
			default:
				return exportOrgs(params, ResultHandler.CONSOLE_EXPORTER, result);
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

	private ExportResult exportOrgs(OrgExportParams params, ResultHandler exportImpl, ExportResult result) throws AppException {
		// We need to clean some Singleton-Instances, as tests are running in the same JVM
		APIManagerAdapter.deleteInstance();
		ErrorState.deleteInstance();
		APIMHttpClient.deleteInstances();
		
		APIManagerAdapter adapter = APIManagerAdapter.getInstance();

		OrgResultHandler exporter = OrgResultHandler.create(exportImpl, params, result);
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
		result.setRc(ErrorState.getInstance().getErrorCode().getCode());
		return result;
	}
	
	@CLIServiceMethod(name = "import", description = "Import organizatio(s) into the API-Manager")
	public static int importOrganization(String[] args) {
		OrgImportParams params;
		try {
			params = new OrgImportCLIOptions(args).getOrgImportParams();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
		OrganizationApp orgApp = new OrganizationApp();
		return orgApp.importOrganization(params);
	}
	
	public int importOrganization(OrgImportParams params) {
		try {
			APIManagerAdapter.deleteInstance();
			ErrorState.deleteInstance();
			APIMHttpClient.deleteInstances();
			
			APIManagerAdapter.getInstance();
			// Load the desired state of the organization
			OrgAdapter orgAdapter = new JSONOrgAdapter(params);
			List<Organization> desiredOrgs = orgAdapter.getOrganizations();
			OrganizationImportManager importManager = new OrganizationImportManager();
			for(Organization desiredOrg : desiredOrgs) {
				Organization actualOrg = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder()
						.hasName(desiredOrg.getName())
						.build());
				importManager.replicate(desiredOrg, actualOrg);
			}
			LOG.info("Successfully replicated organization(s) into API-Manager");
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
			
			OrgExportParams params = new OrgDeleteCLIOptions(args).getOrgExportParams();
			OrganizationApp orgApp = new OrganizationApp();
			return orgApp.delete(params).getRc();
		} catch (AppException e) {
			LOG.error("Error " + e.getMessage());
			return e.getErrorCode().getCode();
		} catch (ParseException e) {
			LOG.error("Error " + e.getMessage());
			return ErrorCode.MISSING_PARAMETER.getCode();
		}
	}
	
	public ExportResult delete(OrgExportParams params) {
		ExportResult result = new ExportResult();
		try {
			return exportOrgs(params, ResultHandler.ORG_DELETE_HANDLER, result);
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

	public static void main(String args[]) { 
		int rc = exportOrgs(args);
		System.exit(rc);
	}


}
