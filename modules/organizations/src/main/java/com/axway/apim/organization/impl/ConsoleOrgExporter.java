package com.axway.apim.organization.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.OrgExportParams;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleOrgExporter extends OrgResultHandler {
	
	APIManagerAdapter adapter;
	
	Map<String, Integer> apiCountPerOrg = null;
	Map<String, Integer> appCountPerOrg = null;
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsoleOrgExporter(OrgExportParams params, ExportResult result) {
		super(params, result);
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void export(List<Organization> orgs) throws AppException {
		switch(params.getWide()) {
		case standard:
			printStandard(orgs);
			break;
		case wide:
			printWide(orgs);
			break;
		case ultra:
			printUltra(orgs);
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
	
	private void printUltra(List<Organization> orgs) {
		System.out.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
				new Column().header("Organization-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getId()),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getName()),
				new Column().header("V-Host").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(org -> org.getVirtualHost()),
				new Column().header("Dev").with(org -> Boolean.toString(org.isDevelopment())),
				new Column().header("Email").with(org -> org.getEmail()),
				new Column().header("Enabled").with(org -> Boolean.toString(org.isEnabled())),
				new Column().header("Created on").with(org -> new Date(org.getCreatedOn()).toString()),
				new Column().header("Restricted").with(org -> Boolean.toString(org.isRestricted())),
				new Column().header("APIs").with(org -> getNoOfAPIsForOrg(org)),
				new Column().header("Apps").with(org -> getNoOfAppsForOrg(org))
				)));
	}

	@Override
	public OrgFilter getFilter() throws AppException {
		return getBaseOrgFilterBuilder().build();
	}
	
	private String getNoOfAPIsForOrg(Organization org) {
		try {
			if(this.apiCountPerOrg==null) {
				this.apiCountPerOrg = new HashMap<String, Integer>();
				List<API> allAPIs = adapter.apiAdapter.getAPIs(new APIFilter.Builder().build(), false);
				for(API api: allAPIs) {
					Integer count = this.apiCountPerOrg.get(api.getOrganization().getName())==null ? 0 : this.apiCountPerOrg.get(api.getOrganization().getName());
					count = new Integer(count.intValue() + 1);
					this.apiCountPerOrg.put(api.getOrganization().getName(), count);
				}
			}
			return this.apiCountPerOrg.get(org.getName())==null ? "N/A" : this.apiCountPerOrg.get(org.getName()).toString();
		} catch (AppException e) {
			return "Err";
		}
	}
	
	private String getNoOfAppsForOrg(Organization org) {
		try {
			if(this.appCountPerOrg==null) {
				this.appCountPerOrg = new HashMap<String, Integer>();
				List<ClientApplication> allAPPs = adapter.appAdapter.getAllApplications(false);
				for(ClientApplication app: allAPPs) {
					Integer count = this.appCountPerOrg.get(app.getOrganization().getName())==null ? 0 : this.appCountPerOrg.get(app.getOrganization().getName());
					count = new Integer(count.intValue() + 1);
					this.appCountPerOrg.put(app.getOrganization().getName(), count);
				}
			}
			return this.appCountPerOrg.get(org.getName())==null ? "N/A" : this.appCountPerOrg.get(org.getName()).toString();
		} catch (AppException e) {
			return "Err";
		}
	}
}
