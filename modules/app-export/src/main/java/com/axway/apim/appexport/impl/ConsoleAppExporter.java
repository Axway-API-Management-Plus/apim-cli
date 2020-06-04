package com.axway.apim.appexport.impl;

import java.util.Arrays;
import java.util.List;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleAppExporter extends ApplicationExporter {

	public ConsoleAppExporter(List<ClientApplication> apps, AppExportParams params) {
		super(apps, params);
	}

	@Override
	public void export() throws AppException {
		System.out.println(AsciiTable.getTable(apps, Arrays.asList(
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(app -> app.getName()),
				new Column().header("State").with(app -> app.getState()),
				new Column().header("Email").with(app -> app.getEmail()),
				new Column().header("Enabled").with(app -> Boolean.toString(app.isEnabled())),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(app -> app.getOrganization().getName()),
				new Column().header("APIs").with(app -> Integer.toString(app.getApiAccess().size())),
				new Column().header("Quotas").with(app -> Boolean.toString(hasAppQuota(app))
				))));
	}
	
	private boolean hasAppQuota(ClientApplication app) {
		return (app.getAppQuota()!=null) && 
				app.getAppQuota().getRestrictions()!=null &&
				app.getAppQuota().getRestrictions().size()>0;
	}
}
