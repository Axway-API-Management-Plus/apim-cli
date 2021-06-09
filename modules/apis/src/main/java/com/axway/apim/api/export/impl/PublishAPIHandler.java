package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class PublishAPIHandler extends APIResultHandler {

	public PublishAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		System.out.println("Going to publish: " + apis.size() + " API(s)");
		for(API api : apis) {
			try {
				statusManager.update(api, API.STATE_PUBLISHED, api.getVhost(), true);
			} catch(Exception e) {
				LOG.error("Error publishing API: " + api.getName());
			}
		}
		System.out.println("Done!");
		return;
	}

	@Override
	public APIFilter getFilter() {
		return getBaseAPIFilterBuilder().build();
	}

}
