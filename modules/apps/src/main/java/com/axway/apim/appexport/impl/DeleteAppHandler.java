package com.axway.apim.appexport.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter.Builder;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAppHandler extends ApplicationExporter {
	private static final Logger LOG = LoggerFactory.getLogger(DeleteAppHandler.class);

	public DeleteAppHandler(AppExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public void export(List<ClientApplication> apps) throws AppException {
		Console.println(apps.size() + " applications selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			Console.println("Force flag given to delete: "+apps.size()+" Application(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				Console.println("Canceled.");
				return;
			}
		}
		Console.println("Okay, going to delete: " + apps.size() + " Application(s)");
		for(ClientApplication app : apps) {
			try {
				APIManagerAdapter.getInstance().appAdapter.deleteApplication(app);
			} catch(Exception e) {
				result.setError(ErrorCode.ERR_DELETING_ORG);
				LOG.error("Error deleting application: {}" , app.getName());
			}
		}
		Console.println("Done!");
	}

	@Override
	public ClientAppFilter getFilter() throws AppException {
		Builder builder = getBaseFilterBuilder();
		return builder.build();
	}
}
