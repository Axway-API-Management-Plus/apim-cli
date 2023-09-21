package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApproveAPIHandler extends APIResultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ApproveAPIHandler.class);

	public ApproveAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		String vhostToUse = ( ((APIApproveParams) params).getPublishVhost()==null) ? "Default" : ((APIApproveParams)params).getPublishVhost();
		Console.println(apis.size() + " API(s) selected for approval/publish on V-Host: "+vhostToUse+".");
		if(CoreParameters.getInstance().isForce()) {
			Console.println("Force flag given to approve/publish: "+apis.size()+" API(s) on V-Host: " + vhostToUse);
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				Console.println("Canceled.");
				return;
			}
		}
		Console.println("Okay, going to approve: " + apis.size() + " API(s) on V-Host: " + vhostToUse);
		for(API api : apis) {
			try {
				APIManagerAdapter.getInstance().getApiAdapter().publishAPI(api, ((APIApproveParams)params).getPublishVhost());
				LOG.info("API: {} {} {} successfully approved/published.", api.getName(), api.getVersion(), api.getId());
			} catch(Exception e) {
				LOG.error("Error approving API: {} {} {} " , api.getName(), api.getVersion(), api.getId());
				result.setError(ErrorCode.ERR_APPROVING_API);
			}
		}
		Console.println("Done!");
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		builder.hasState(API.STATE_PENDING);
		return builder.build();
	}

}
