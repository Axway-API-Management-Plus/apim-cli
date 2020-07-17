package com.axway.apim.organization.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.OrgExportParams;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleOrgExporter extends OrgResultHandler {
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsoleOrgExporter(OrgExportParams params) {
		super(params);
	}

	@Override
	public void export(List<Organization> orgs) throws AppException {
		switch(params.getWide()) {
		case standard:
			printStandard(orgs);
			break;
		case wide:
		case ultra:
			printWide(orgs);
		}
	}
	
	private void printStandard(List<Organization> orgs) {
		System.out.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
				new Column().header("Organization-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getId()),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getName()),
				new Column().header("V-Host").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getVirtualHost()),
				new Column().header("Dev").with(org -> Boolean.toString(org.isDevelopment())),
				new Column().header("Email").with(org -> org.getEmail()),
				new Column().header("Enabled").with(org -> Boolean.toString(org.isEnabled()))
				)));
	}
	
	private void printWide(List<Organization> orgs) {
		System.out.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
				new Column().header("Organization-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getId()),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getName()),
				new Column().header("V-Host").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getVirtualHost()),
				new Column().header("Dev").with(org -> Boolean.toString(org.isDevelopment())),
				new Column().header("Email").with(org -> org.getEmail()),
				new Column().header("Enabled").with(org -> Boolean.toString(org.isEnabled())),
				new Column().header("Created on").with(org -> new Date(org.getCreatedOn()).toString()),
				new Column().header("Restricted").with(org -> Boolean.toString(org.isRestricted()))
				)));
	}

	@Override
	public OrgFilter getFilter() throws AppException {
		return getBaseOrgFilterBuilder().build();
	}
}
