package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishAPIHandler extends APIResultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(PublishAPIHandler.class);

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
				result.setError(ErrorCode.ERR_PUBLISH_API);
				LOG.error("Error publishing API: " + api.getName());
			}
		}
		System.out.println("Done!");
	}

	@Override
	public APIFilter getFilter() {
		return getBaseAPIFilterBuilder().build();
	}

}
