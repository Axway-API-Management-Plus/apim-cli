package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RevokeAccessAPIHandler extends APIResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RevokeAccessAPIHandler.class);
    List<API> apis;
    List<Organization> orgs;

    public RevokeAccessAPIHandler(APIExportParams params) {
        super(params);
        APIGrantAccessParams grantAccessParams = (APIGrantAccessParams) params;
        this.apis = grantAccessParams.getApis();
        this.orgs = grantAccessParams.getOrgs();
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        if (apis == null || apis.isEmpty()) {
            throw new AppException("API to revoke access  is missing.", ErrorCode.UNKNOWN_API);
        }
        if (orgs == null || orgs.isEmpty()) {
            throw new AppException("Organization to revoke access to is missing.", ErrorCode.UNKNOWN_ORGANIZATION);
        }
        for (API api : apis) {
            try {
                APIManagerAdapter.getInstance().apiAdapter.revokeClientOrganization(orgs, api);
                LOG.info("API: {} revoked access to organization: {}", api.toStringHuman(), orgs);
            } catch (Exception e) {
                result.setError(ErrorCode.ERR_GRANTING_ACCESS_TO_API);
                LOG.error("Error revoking access to API:  {}  for organizations: {} Error message: {}", api.toStringHuman(), orgs, e.getMessage());
            }
        }
    }

    @Override
    public APIFilter getFilter() {
        APIFilter.Builder builder = getBaseAPIFilterBuilder();
        builder.hasState(API.STATE_PUBLISHED);
        return builder.build();
    }
}
