package com.axway.apim.appexport.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.clientApps.ClientAppFilter.Builder;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public class DeleteAppHandler extends ApplicationExporter {

	public DeleteAppHandler(AppExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public void export(List<ClientApplication> apps) throws AppException {
		System.out.println(apps.size() + " applications selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to delete: "+apps.size()+" Application(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to delete: " + apps.size() + " Application(s)");
		for(ClientApplication app : apps) {
			try {
				APIManagerAdapter.getInstance().appAdapter.deleteApplication(app);
			} catch(Exception e) {
				result.setError(ErrorCode.ERR_DELETING_ORG);
				LOG.error("Error deleting application: " + app.getName());
			}
		}
		System.out.println("Done!");
	}

	@Override
	public ClientAppFilter getFilter() throws AppException {
		Builder builder = getBaseFilterBuilder();
		return builder.build();
	}
}
