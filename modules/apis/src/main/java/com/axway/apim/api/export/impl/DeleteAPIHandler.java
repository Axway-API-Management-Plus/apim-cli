package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeleteAPIHandler extends APIResultHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DeleteAPIHandler.class);
	public DeleteAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		Console.println(apis.size() + " selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			Console.println("Force flag given to delete: "+apis.size()+" API(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				Console.println("Canceled.");
				return;
			}
		}
		Console.println("Okay, going to delete: " + apis.size() + " API(s)");
		for(API api : apis) {
			try {
				statusManager.update(api, API.STATE_DELETED, true);
			} catch(Exception e) {
				result.setError(ErrorCode.ERR_DELETING_API);
				LOG.error("Error deleting API: {}" , api.getName());
			}
		}
		Console.println("Done!");

	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}

}
