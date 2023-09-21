package com.axway.apim.organization.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.OrgFilter.Builder;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.organization.lib.OrgExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteOrgHandler extends OrgResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteOrgHandler.class);


    public DeleteOrgHandler(OrgExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<Organization> orgs) throws AppException {
        Console.println(orgs.size() + " selected for deletion.");
        if (CoreParameters.getInstance().isForce()) {
            Console.println("Force flag given to delete: " + orgs.size() + " Organization(s)");
        } else {
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
                Console.println("Okay, going to delete: " + orgs.size() + " Organization(s)");
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        for (Organization org : orgs) {
            try {
                APIManagerAdapter.getInstance().getOrgAdapter().deleteOrganization(org);
            } catch (Exception e) {
                result.setError(ErrorCode.ERR_DELETING_ORG);
                LOG.error("Error deleting Organization: {}", org.getName());
            }
        }
        Console.println("Done!");
    }

    @Override
    public OrgFilter getFilter() {
        Builder builder = getBaseOrgFilterBuilder();
        return builder.build();
    }
}
