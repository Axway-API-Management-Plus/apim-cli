package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;

public class DeleteAPIHandler extends APIResultHandler {

	public DeleteAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		System.out.println(apis.size() + " selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to delete: "+apis.size()+" API(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to delete: " + apis.size() + " API(s)");
		for(API api : apis) {
			try {
				statusManager.update(api, API.STATE_DELETED, true);
			} catch(Exception e) {
				LOG.error("Error deleting API: " + api.getName());
			}
		}
		System.out.println("Done!");

	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}

}
