package com.axway.apim.api.export.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.ActionResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public class ApproveAPIHandler extends APIResultHandler {

	public ApproveAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public ActionResult execute(List<API> apis) throws AppException {
		ActionResult result = new ActionResult();
		String vhostToUse = ( ((APIApproveParams) params).getPublishVhost()==null) ? "Default" : ((APIApproveParams)params).getPublishVhost();
		System.out.println(apis.size() + " API(s) selected for approval/publish on V-Host: "+vhostToUse+".");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to approve/publish: "+apis.size()+" API(s) on V-Host: " + vhostToUse);
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return result;
			}
		}
		System.out.println("Okay, going to approve: " + apis.size() + " API(s) on V-Host: " + vhostToUse);
		for(API api : apis) {
			try {
				APIManagerAdapter.getInstance().apiAdapter.publishAPI(api, ((APIApproveParams)params).getPublishVhost());
				LOG.info("API: "+api.getName()+" "+api.getVersion()+" ("+api.getId()+") successfully approved/published.");
			} catch(Exception e) {
				LOG.error("Error approving API: " + api.getName()+" "+api.getVersion()+" ("+api.getId()+")");
				result.setError("Error approving API: " + api.getName()+" "+api.getVersion()+" ("+api.getId()+")", ErrorCode.ERR_APPROVING_API);
			}
		}
		System.out.println("Done!");
		return result;
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		builder.hasState(API.STATE_PENDING);
		return builder.build();
	}

}
