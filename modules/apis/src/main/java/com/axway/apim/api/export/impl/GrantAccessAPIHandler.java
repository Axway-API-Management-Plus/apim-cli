package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GrantAccessAPIHandler extends APIResultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GrantAccessAPIHandler.class);
	List<API> apis;
	List<Organization> orgs;

	public GrantAccessAPIHandler(APIExportParams params) {
		super(params);
		APIGrantAccessParams grantAccessParams = (APIGrantAccessParams)params;
		this.apis = grantAccessParams.getApis();
		this.orgs = grantAccessParams.getOrgs();
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		if(apis == null || apis.size() == 0) {
			throw new AppException("List of APIs to grant access to is missing.", ErrorCode.UNKNOWN_API);
		}
		if(orgs == null || orgs.size() == 0) {
			throw new AppException("List of Orgs to grant access to is missing.", ErrorCode.UNKNOWN_ORGANIZATION);
		}
		if(apis.size()==1) {
			System.out.println("Selected organizations: " + orgs + " get access to API: "+apis.get(0).toStringHuman());
		} else {
			System.out.println("Selected organizations: " + orgs + " get access to "+apis.size()+" selected APIs.");
		}
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to grant access for selected organizations to: "+apis.size()+" API(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to grant access to: " + apis.size() + " API(s) for "+orgs.size()+" organizations.");
		for(API api : apis) {
			try {
				APIManagerAdapter.getInstance().apiAdapter.grantClientOrganization(orgs, api, hasError);
				LOG.info("API: {} granted access to orgs: {}",api.toStringHuman(), orgs);
			} catch(Exception e) {
				result.setError(ErrorCode.ERR_GRANTING_ACCESS_TO_API);
				LOG.error("Error granting access to API:  {}  for organizations: {} Error message: {}" ,api.toStringHuman(), orgs, e.getMessage());
			}
		}
		System.out.println("Done!");
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		builder.hasState(API.STATE_PUBLISHED);
		return builder.build();
	}
}
