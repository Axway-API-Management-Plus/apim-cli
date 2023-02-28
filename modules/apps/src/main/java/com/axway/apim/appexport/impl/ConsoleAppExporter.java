package com.axway.apim.appexport.impl;

import java.util.Arrays;
import java.util.List;

import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter.Builder;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleAppExporter extends ApplicationExporter {

	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsoleAppExporter(AppExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public void export(List<ClientApplication> apps) throws AppException {
		switch(params.getWide()) {
		case standard:
			printStandard(apps);
			break;
		case wide:
			printWide(apps);
			break;
		case ultra:
			printUltra(apps);
			break;
		}
	}
	
	private void printStandard(List<ClientApplication> apps) {
		Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
				new Column().header("Application-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
				new Column().header("State").with(app -> app.getState().name()),
				new Column().header("Email").with(ClientApplication::getEmail),
				new Column().header("Enabled").with(app -> Boolean.toString(app.isEnabled())),
				new Column().header("Created by").with(app -> getCreatedBy(app.getCreatedBy(), app)
				))));
	}
	
	private void printWide(List<ClientApplication> apps) {
		Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
				new Column().header("Application-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
				new Column().header("State").with(app -> app.getState().name()),
				new Column().header("Email").with(ClientApplication::getEmail),
				new Column().header("Enabled").with(app -> Boolean.toString(app.isEnabled())),
				new Column().header("Created by").with(app -> getCreatedBy(app.getCreatedBy(), app)),
				new Column().header("Created on").with(app -> getCreatedOn(app.getCreatedOn()).toString()),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(app -> app.getOrganization().getName())
				)));
	}
	
	private void printUltra(List<ClientApplication> apps) {
		Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
				new Column().header("Application-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
				new Column().header("State").with(app -> app.getState().name()),
				new Column().header("Email").with(ClientApplication::getEmail),
				new Column().header("Enabled").with(app -> Boolean.toString(app.isEnabled())),
				new Column().header("Created by").with(app -> getCreatedBy(app.getCreatedBy(), app)),
				new Column().header("Created on").with(app -> getCreatedOn(app.getCreatedOn()).toString()),
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

	@Override
	public ClientAppFilter getFilter() throws AppException {
		Builder builder = getBaseFilterBuilder();
		
		switch(params.getWide()) {
		case standard:
			builder.includeQuotas(false);
			if(params.getCredential()==null && params.getRedirectUrl()==null) builder.includeCredentials(false);
			if(params.getApiName()==null) builder.includeAPIAccess(false);
			break;
		case wide:
			if(params.getCredential()==null && params.getRedirectUrl()==null) builder.includeCredentials(false);
			builder.includeQuotas(false);
			builder.includeAPIAccess(false);
			break;
		case ultra:
			builder.includeQuotas(true);
			if(params.getCredential()==null && params.getRedirectUrl()==null) builder.includeCredentials(true);
			builder.includeAPIAccess(true);
			break;
		}
		return builder.build();
	}
}
