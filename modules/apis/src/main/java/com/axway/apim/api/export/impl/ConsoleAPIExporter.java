package com.axway.apim.api.export.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleAPIExporter extends APIResultHandler {
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsoleAPIExporter(APIExportParams params) {
		super(params);
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		switch(params.getWide()) {
		case standard:
			printStandard(apis);
			break;
		case wide:
			printWide(apis);
			break;
		case ultra:
			printUltra(apis);
			break;
		}
		return;
	}
	
	private void printStandard(List<API> apis) {
		System.out.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
				new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getId()),
				new Column().header("Path").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> getPath(api)),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getName()),
				new Column().header("Version").with(api -> api.getVersion()
				))));
		printDetails(apis);
	}
	
	private void printWide(List<API> apis) {
		System.out.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
				new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getId()),
				new Column().header("Path").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> getPath(api)),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getName()),
				new Column().header("Version").with(api -> api.getVersion()),
				new Column().header("V-Host").with(api -> api.getVhost()),
				new Column().header("State").with(api -> getState(api)),
				new Column().header("Backend").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> getBackendPath(api)),
				new Column().header("Security").with(api -> getUsedSecurity(api)),
				new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> getUsedPoliciesForConsole(api).toString()),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()
				))));
		printDetails(apis);
	}
	
	private void printUltra(List<API> apis) {
		System.out.println(AsciiTable.getTable(borderStyle, apis, Arrays.asList(
				new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getId()),
				new Column().header("Path").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> getPath(api)),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getName()),
				new Column().header("Version").with(api -> api.getVersion()),
				new Column().header("V-Host").with(api -> api.getVhost()),
				new Column().header("State").with(api -> getState(api)),
				new Column().header("Backend").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> getBackendPath(api)),
				new Column().header("Security").with(api -> getUsedSecurity(api)),
				new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> getUsedPoliciesForConsole(api).toString()),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()),
				new Column().header("Orgs").with(api -> getOrgCount(api)),
				new Column().header("Apps").with(api -> getAppCount(api)),
				new Column().header("Quotas").with(api -> Boolean.toString(hasQuota(api))),
				new Column().header("Tags").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> Boolean.toString(hasTags(api)))
				)));
		printDetails(apis);
	}
	
	private String getUsedPoliciesForConsole(API api) {
		List<String> usedPolicies = new ArrayList<String>();
		Map<PolicyType, List<String>> allPolicies = getUsedPolicies(api);
		for(List<String> policyNames : allPolicies.values()) {
			for(String polName : policyNames) {
				if(usedPolicies.contains(polName)) continue;
				usedPolicies.add(polName);
			}
		}
		return usedPolicies.toString().replace("[", "").replace("]", "");
	}
	
	private void printDetails(List<API> apis) {
		if(apis.size()!=1) return;
		API api = apis.get(0);
		// If wide isn't ultra, we have to reload some more information for the detail view
		if(!params.getWide().equals(Wide.ultra)) {
			try {
				APIManagerAdapter.getInstance().apiAdapter.addClientApplications(api);
				APIManagerAdapter.getInstance().apiAdapter.addClientOrganizations(api);
				APIManagerAdapter.getInstance().apiAdapter.addQuotaConfiguration(api);
			} catch (AppException e) {
				LOG.error("Error loading API details. " + e.getMessage());
			}
		}
		System.out.println();
		System.out.println("A P I  -  D E T A I L S");
		System.out.println(String.format("%-25s", "Organization: ") + api.getOrganization().getName());
		System.out.println(String.format("%-25s", "Created On: ") + new Date(api.getCreatedOn()));
		System.out.println(String.format("%-25s", "Created By: ") + getCreatedBy(api));
		System.out.println(String.format("%-25s", "Granted Organizations: ") + getGrantedOrganizations(api).toString().replace("[", "").replace("]", ""));
		System.out.println(String.format("%-25s", "Subscribed applications: ") + getSubscribedApplications(api));
		System.out.println(String.format("%-25s", "Custom-Policies: ") + getUsedPolicies(api));
		System.out.println(String.format("%-25s", "Tags: ") + getTags(api));
		System.out.println(String.format("%-25s", "Custom-Properties: ") + getCustomProps(api));
	}
	
	private boolean hasQuota(API api) {
		return (api.getApplicationQuota()!=null && 
				api.getApplicationQuota().getRestrictions()!=null &&
				api.getApplicationQuota().getRestrictions().size()>0) || 
				(api.getSystemQuota()!=null && 
				api.getSystemQuota().getRestrictions()!=null &&
				api.getSystemQuota().getRestrictions().size()>0)
				;
	}
	
	private String getState(API api) {
		return api.getState();
	}
	
	private String getCreatedBy(API api) {
		try {
			return APIManagerAdapter.getInstance().userAdapter.getUserForId(api.getCreatedBy()).getName();
		} catch (Exception e) {
			LOG.error("Error getting created by user");
			return "Err";
		}
	}
	
	private String getPath(API api) {
		return api.getPath();
	}
	
	private boolean hasTags(API api) {
		return (api.getTags()!=null && api.getTags().size()!=0);
	}
	
	private String getOrgCount(API api) {
		if(api.getClientOrganizations()==null) return "N/A";
		return Integer.toString(api.getClientOrganizations().size());
	}
	
	private String getAppCount(API api) {
		if(api.getApplications()==null) return "N/A";
		return Integer.toString(api.getApplications().size());
	}
	
	private String getSubscribedApplications(API api) {
		if(api.getApplications()==null) return "N/A";
		List<String> subscribedApps = new ArrayList<String>();
		for(ClientApplication app : api.getApplications()) {
			subscribedApps.add(app.getName());
		}
		return subscribedApps.toString().replace("[", "").replace("]", "");
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();

		switch(params.getWide()) {
		case standard:
		case wide:
			builder.includeQuotas(false);
			builder.includeClientApplications(false);
			builder.includeClientOrganizations(false);
			builder.includeClientAppQuota(false);
			builder.includeQuotas(false);
			break;
		case ultra:
			builder.includeQuotas(true);
			builder.includeClientAppQuota(false);
			builder.includeClientApplications(true);
			builder.includeClientOrganizations(true);
			break;
		}		
		APIFilter filter = builder.build();
		return filter;
	}
}
