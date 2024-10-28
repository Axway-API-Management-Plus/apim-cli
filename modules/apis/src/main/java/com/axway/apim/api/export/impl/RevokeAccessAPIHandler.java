package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Constants;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RevokeAccessAPIHandler extends APIResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RevokeAccessAPIHandler.class);
    List<API> apis;
    List<Organization> orgs;

    private final ClientApplication clientApplication;

    public RevokeAccessAPIHandler(APIExportParams params) {
        super(params);
        APIGrantAccessParams grantAccessParams = (APIGrantAccessParams) params;
        this.apis = grantAccessParams.getApis();
        this.orgs = grantAccessParams.getOrgs();
        this.clientApplication = grantAccessParams.getClientApplication();
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        validate(apis);
        if (!CoreParameters.getInstance().isForce()) {
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
                Console.println("Going to revoke access.");
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        revokeAccess(apis);
    }

    public void validate(List<API> apis) throws AppException{
        if (apis == null || apis.isEmpty()) {
            throw new AppException("API to revoke access  is missing.", ErrorCode.UNKNOWN_API);
        }
        if (orgs == null || orgs.isEmpty()) {
            throw new AppException("Organization to revoke access is missing.", ErrorCode.UNKNOWN_ORGANIZATION);
        }
    }

    public void revokeAccess(List<API> apis) throws AppException{
        APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
        for (API api : apis) {
            try {
                if (clientApplication == null) {
                    apiAdapter.revokeClientOrganization(orgs, api);
                    if (LOG.isDebugEnabled())
                        LOG.debug("API: {} revoked access to organization: {}", api.toStringHuman(), orgs);
                } else {
                    boolean deleteFlag = false;
                    for (Organization organization : orgs) {
                        LOG.debug("{} {}", clientApplication.getOrganizationId(), organization.getId());
                        if (clientApplication.getOrganizationId().equals(organization.getId())) {
                            List<APIAccess> apiAccesses = APIManagerAdapter.getInstance().getAccessAdapter().getAPIAccess(clientApplication, APIManagerAPIAccessAdapter.Type.applications);
                            if (apiAccesses.isEmpty()) {
                                throw new AppException(String.format("Application %s is not associated with API %s", clientApplication.getName(), api.getName()), ErrorCode.REVOKE_ACCESS_APPLICATION_ERR);
                            }
                            clientApplication.setApiAccess(apiAccesses);
                            apiAdapter.revokeClientApplication(clientApplication, api);
                            if (LOG.isDebugEnabled())
                                LOG.debug("API: {} revoked access to application: {}", api.toStringHuman(), clientApplication);
                            deleteFlag = true;
                            break;
                        }
                    }
                    if (!deleteFlag) {
                        throw new AppException("Application " + clientApplication.getName() + " Does not belong to organization " + orgs, ErrorCode.UNXPECTED_ERROR);
                    }
                }
            } catch (AppException e) {
                result.setError(e.getError());
                LOG.error("Error revoking access to API : {}", api.toStringHuman(), e);
            } catch (Exception e) {
                result.setError(ErrorCode.ERR_GRANTING_ACCESS_TO_API);
                LOG.error("Error revoking access to API:  {}  for organizations: {} Error message: {}", api.toStringHuman(), orgs, e.getMessage());
            }
        }
    }

    @Override
    public APIFilter getFilter() {
        APIFilter.Builder builder = getBaseAPIFilterBuilder();
        builder.hasState(Constants.API_PUBLISHED);
        return builder.build();
    }
}
