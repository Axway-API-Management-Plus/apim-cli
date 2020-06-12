package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class UnpublishAPIHandler extends APIResultHandler {

	public UnpublishAPIHandler(APIExportParams params) {
		super(params);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		System.out.println(apis.size() + " selected to unpublish.");
		if(askYesNo("Do you wish to proceed? (Y/N)")) {
			System.out.println("Okay, going to unpublish: " + apis.size());
			for(API api : apis) {
				api.setState(API.STATE_UNPUBLISHED);
				APIManagerAdapter.getInstance().apiAdapter.updateAPIStatus(api);
			}
		}
	}

	@Override
	public APIFilter getFilter() {
		return getBaseAPIFilterBuilder().build();
	}

}
