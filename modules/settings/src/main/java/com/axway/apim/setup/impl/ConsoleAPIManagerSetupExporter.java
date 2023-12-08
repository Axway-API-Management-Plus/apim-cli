package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.setup.APIManagerSettingsApp;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;

public class ConsoleAPIManagerSetupExporter extends APIManagerSetupResultHandler {

	APIManagerAdapter adapter;

	public ConsoleAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) throws AppException {
		super(params, result);
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new AppException("Unable to get APIManagerAdapter", ErrorCode.UNXPECTED_ERROR);
		}
	}

	@Override
	public RemoteHostFilter getRemoteHostFilter() {
		return getRemoteHostBaseFilterBuilder().build();
	}

	@Override
	public void export(APIManagerConfig config) throws AppException {
		if(params.isExportConfig()) {
			ConsolePrinterConfig configExporter = new ConsolePrinterConfig(params);
			configExporter.export(config.getConfig());
		}

		if(params.isExportAlerts()) {
			ConsolePrinterAlerts alertsExporter = new ConsolePrinterAlerts();
			alertsExporter.export(config.getAlerts());
		}

		if(params.isExportRemoteHosts()) {
			ConsolePrinterRemoteHosts remoteHostsExporter = new ConsolePrinterRemoteHosts(params);
			remoteHostsExporter.export(config.getRemoteHosts());
		}

		if(params.isExportPolicies()) {
			ConsolePrinterPolicies policiesPrinter = new ConsolePrinterPolicies();
			policiesPrinter.export(adapter.getPoliciesAdapter().getAllPolicies());
		}

		if(params.isExportCustomProperties()) {
			Console.println("Configured custom properties for: '" + APIManagerAdapter.getInstance().getApiManagerName() + "' Version: " + APIManagerAdapter.getInstance().getApiManagerVersion());
			ConsolePrinterCustomProperties propertiesPrinter = new ConsolePrinterCustomProperties();
			for(Type type: Type.values()) {
				propertiesPrinter.addProperties(adapter.getCustomPropertiesAdapter().getCustomProperties(type), type);
			}
			propertiesPrinter.printCustomProperties();
		}

        if(params.isExportQuotas()) {
            ConsolePrinterGlobalQuotas policiesPrinter = new ConsolePrinterGlobalQuotas();
            policiesPrinter.export(new APIManagerSettingsApp().getGlobalQuotas(adapter));
        }
	}
}
