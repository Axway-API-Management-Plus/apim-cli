package com.axway.apim.api.export.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public class UpgradeAccessAPIHandler extends APIResultHandler {

	public UpgradeAccessAPIHandler(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		APIUpgradeAccessParams upgradeParams = (APIUpgradeAccessParams) params;
		API referenceAPI = upgradeParams.getReferenceAPI();
		if(referenceAPI == null) {
			throw new AppException("Reference API for upgrade is missing.", ErrorCode.UNKNOWN_API);
		}
		System.out.println(apis.size() + " API(s) selected for upgrade based on reference/old API: "+referenceAPI.getName()+" "+referenceAPI.getVersion()+" ("+referenceAPI.getId()+").");		
		System.out.println("Old/Reference API: deprecate: " + upgradeParams.getReferenceAPIDeprecate() + ", retired: " + upgradeParams.getReferenceAPIRetire() + ", retirementDate: " + getRetirementDate(upgradeParams.getReferenceAPIRetirementDate()));
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to upgrade: "+apis.size()+" API(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to upgrade: " + apis.size() + " API(s) based on reference/old API: "+referenceAPI.getName()+" "+referenceAPI.getVersion()+" ("+referenceAPI.getId()+").");
		for(API api : apis) {
			try {
				if(APIManagerAdapter.getInstance().apiAdapter.upgradeAccessToNewerAPI(api, referenceAPI, 
						upgradeParams.getReferenceAPIDeprecate(), upgradeParams.getReferenceAPIRetire(), upgradeParams.getReferenceAPIRetirementDate())) {
					LOG.info("API: "+api.getName()+" "+api.getVersion()+" ("+api.getId()+") successfully upgraded.");
				}
			} catch(Exception e) {
				LOG.error("Error upgrading API: " + api.getName()+" "+api.getVersion()+" ("+api.getId()+") Error message: " + e.getMessage());
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
	
	private String getRetirementDate(Long retirementDate) {
		if(retirementDate == null) return "N/A";
		Date retireDate = new Date(retirementDate);
		return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(retireDate);
	}

}
