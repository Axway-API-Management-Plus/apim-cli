package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UnPublishAPIHandler extends APIResultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(UnPublishAPIHandler.class);
	public UnPublishAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		System.out.println(apis.size() + " selected to unpublish.");
		if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			System.out.println("Okay, going to unpublish: " + apis.size() + " API(s)");
			for(API api : apis) {
				try {
					statusManager.update(api, API.STATE_UNPUBLISHED, true);
				} catch(Exception e) {
					result.setError(ErrorCode.ERR_UNPUBSLISH_API);
					LOG.error("Error unpublishing API: " + api.getName());
				}
			}
			System.out.println("Done!");
		}
	}

	@Override
	public APIFilter getFilter() {
		return getBaseAPIFilterBuilder().build();
	}

}
