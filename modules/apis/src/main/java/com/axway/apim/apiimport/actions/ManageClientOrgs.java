package com.axway.apim.apiimport.actions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ManageClientOrgs {

    static Logger LOG = LoggerFactory.getLogger(ManageClientOrgs.class);

    APIManagerAdapter apiManager;

    private final API desiredState;
    private final API actualState;

    public ManageClientOrgs(API desiredState, API actualState) throws AppException {
        this.desiredState = desiredState;
        this.actualState = actualState;
        apiManager = APIManagerAdapter.getInstance();

    }

    public void execute(boolean reCreation) throws AppException {
        if (CoreParameters.getInstance().isIgnoreClientOrgs()) {
            LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
            return;
        }
        if (desiredState.getState().equals(API.STATE_UNPUBLISHED)) return;
        // The API isn't Re-Created (to take over manually created ClientOrgs) and there are no orgs configured - We can skip the rest
        if (desiredState.getClientOrganizations() == null && !reCreation) return;
        // From here, the assumption is that existing Org-Access has been upgraded already - We only have to take care about additional orgs
        if ((desiredState).isRequestForAllOrgs()) {
            LOG.info("Granting permission to all organizations");
            apiManager.apiAdapter.grantClientOrganization(getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations()), actualState, true);
        } else {
            List<Organization> missingDesiredOrgs = getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations());
            List<Organization> removingActualOrgs = getMissingOrgs(actualState.getClientOrganizations(), desiredState.getClientOrganizations());
            removingActualOrgs.remove(desiredState.getOrganization());// Don't try to remove the Owning-Organization
            if (missingDesiredOrgs.size() == 0) {
                if (desiredState.getClientOrganizations() != null) {
                    LOG.info("All desired organizations: {} have already access. Nothing to do.", desiredState.getClientOrganizations());
                }
            } else {
                apiManager.apiAdapter.grantClientOrganization(missingDesiredOrgs, actualState, false);
            }
            if (removingActualOrgs.size() > 0) {
                if (CoreParameters.getInstance().getClientOrgsMode().equals(CoreParameters.Mode.replace)) {
                    LOG.info("Removing access for orgs: {} from API: {}", removingActualOrgs, actualState.getName());
                    apiManager.accessAdapter.removeClientOrganization(removingActualOrgs, actualState.getId());
                } else {
                    LOG.info("NOT removing access for existing orgs: {} from API: {} as clientOrgsMode NOT set to replace.",removingActualOrgs,actualState.getName());
                }
            }
        }
    }

    private List<Organization> getMissingOrgs(List<Organization> orgs, List<Organization> referenceOrgs) throws AppException {
        List<Organization> missingOrgs = new ArrayList<>();
        if (orgs == null) return missingOrgs;
        if (referenceOrgs == null) return orgs; // Take over all orgs as missing
        for (Organization org : orgs) {
            if (referenceOrgs.contains(org)) {
                continue;
            }
            Organization organization = apiManager.orgAdapter.getOrgForName(org.getName());
            if (organization == null) {
                LOG.warn("Configured organizations: {}", apiManager.orgAdapter.getAllOrgs());
                throw new AppException("Unknown Org-Name: '" + org.getName() + "'", ErrorCode.UNKNOWN_ORGANIZATION);
            }
            missingOrgs.add(organization);
        }
        return missingOrgs;
    }
}
