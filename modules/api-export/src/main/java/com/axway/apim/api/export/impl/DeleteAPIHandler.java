package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class DeleteAPIHandler extends APIResultHandler {

	public DeleteAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		System.out.println(apis.size() + " selected for deletion.");
		if(askYesNo("Do you wish to proceed? (Y/N)")) {
			System.out.println("Okay, going to delete: " + apis.size() + " API(s)");
			for(API api : apis) {
				try {
					statusManager.update(api, API.STATE_DELETED, true);
				} catch(Exception e) {
					LOG.error("Error deleting API: " + api.getName());
				}
			}
			System.out.println("Done!");
		} else {
			System.out.println("Canceled.");
		}
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}

}
