package com.axway.apim.organization.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.OrgFilter.Builder;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.lib.OrgExportParams;

public class DeleteOrgHandler extends OrgResultHandler {

	public DeleteOrgHandler(OrgExportParams params) {
		super(params);
	}

	@Override
	public void export(List<Organization> orgs) throws AppException {
		System.out.println(orgs.size() + " selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to delete: "+orgs.size()+" Organization(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to delete: " + orgs.size() + " Organization(s)");
		for(Organization org : orgs) {
			try {
				APIManagerAdapter.getInstance().orgAdapter.deleteOrganization(org);
			} catch(Exception e) {
				LOG.error("Error deleting Organization: " + org.getName());
			}
		}
		System.out.println("Done!");

	}

	@Override
	public OrgFilter getFilter() {
		Builder builder = getBaseOrgFilterBuilder();
		return builder.build();
	}
}
