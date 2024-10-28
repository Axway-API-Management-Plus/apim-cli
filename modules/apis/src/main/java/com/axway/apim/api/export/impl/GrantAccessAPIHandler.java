package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
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

public class GrantAccessAPIHandler extends APIResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GrantAccessAPIHandler.class);
    List<API> apis;
    List<Organization> orgs;
    private final ClientApplication clientApplication;


    public GrantAccessAPIHandler(APIExportParams params) {
        super(params);
        APIGrantAccessParams grantAccessParams = (APIGrantAccessParams) params;
        this.apis = grantAccessParams.getApis();
        this.orgs = grantAccessParams.getOrgs();
        this.clientApplication = grantAccessParams.getClientApplication();
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        if (apis == null || apis.isEmpty()) {
            throw new AppException("List of APIs to grant access to is missing.", ErrorCode.UNKNOWN_API);
        }
        if (orgs == null || orgs.isEmpty()) {
            throw new AppException("List of Orgs to grant access to is missing.", ErrorCode.UNKNOWN_ORGANIZATION);
        }
        if (!CoreParameters.getInstance().isForce()) {
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
                Console.println("Going to grant access");
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
        for (API api : apis) {
            try {
                if (clientApplication == null) {
                    apiAdapter.grantClientOrganization(orgs, api, false);
                    if (LOG.isDebugEnabled())
                        LOG.debug("API: {} granted access to orgs: {}", api.toStringHuman(), orgs);
                } else {
                    boolean deleteFlag = false;
                    for (Organization organization : orgs) {
                        List<APIAccess> apiAccesses = APIManagerAdapter.getInstance().getAccessAdapter().getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations);
                        for (APIAccess apiAccess : apiAccesses) {
                            LOG.debug("{} {}", apiAccess.getApiId(), api.getId());
                            if (apiAccess.getApiId().equals(api.getId())) {
                                apiAdapter.grantClientApplication(clientApplication, api);
                                if (LOG.isDebugEnabled())
                                    LOG.debug("API: {} granted access to application: {}", api.toStringHuman(), clientApplication);
                                deleteFlag = true;
                                break;
                            }
                        }
                    }
                    if (!deleteFlag) {
                        throw new AppException("API " + api.getName() + " Does not belong to organization " + orgs, ErrorCode.UNXPECTED_ERROR);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error grant access to API", e);
                if (e instanceof AppException) {
                    result.setError(((AppException) e).getError());
                } else {
                    result.setError(ErrorCode.ERR_GRANTING_ACCESS_TO_API);
                    LOG.error("Error granting access to API:  {}  for organizations: {} Error message: {}", api.toStringHuman(), orgs, e.getMessage());
                }
            }
        }
    }

    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder();
        builder.hasState(Constants.API_PUBLISHED);
        return builder.build();
    }
}
