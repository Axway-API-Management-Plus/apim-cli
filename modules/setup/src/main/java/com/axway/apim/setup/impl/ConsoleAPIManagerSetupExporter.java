package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;

public class ConsoleAPIManagerSetupExporter extends APIManagerSetupResultHandler {
	
	APIManagerAdapter adapter;

	public ConsoleAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) {
		super(params, result);
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public RemoteHostFilter getRemoteHostFilter() throws AppException {
		return getRemoteHostBaseFilterBuilder().build();
	}

	@Override
	public void export(APIManagerConfig config) throws AppException {
		System.out.println();
		System.out.println("Setup for: '" + APIManagerAdapter.getApiManagerName() + "' Version: " + APIManagerAdapter.getApiManagerVersion());
		System.out.println();
		if(config.getConfig()!=null) {
			ConsolePrinterConfig configExporter = new ConsolePrinterConfig(params);
			configExporter.export(config.getConfig());
		}

		if(config.getAlerts()!=null) {
			ConsolePrinterAlerts alertsExporter = new ConsolePrinterAlerts(params);
			alertsExporter.export(config.getAlerts());
		}
		
		if(config.getRemoteHosts()!=null) {
			ConsolePrinterRemoteHosts remoteHostsExporter = new ConsolePrinterRemoteHosts(params);
			remoteHostsExporter.export(config.getRemoteHosts());
		}
	}
}
