package com.axway.apim.api.export.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
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
				new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> getUsedPolicies(api)),
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
				new Column().header("Policies").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> getUsedPolicies(api)),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()),
				new Column().header("Orgs").with(api -> getOrgCount(api)),
				new Column().header("Apps").with(api -> getAppCount(api)),
				new Column().header("Quotas").with(api -> Boolean.toString(hasQuota(api))),
				new Column().header("Tags").dataAlign(HorizontalAlign.LEFT).maxColumnWidth(30).with(api -> Boolean.toString(hasTags(api)))
				)));
		printDetails(apis);
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
		System.out.println(String.format("%-25s", "Granted Organizations: ") + getGrantedOrganizations(api));
		System.out.println(String.format("%-25s", "Subscribed applications: ") + getSubscribedApplications(api));
		System.out.println(String.format("%-25s", "Custom-Policies: ") + getUsedPolicies(api, true));
		System.out.println(String.format("%-25s", "Tags: ") + getTags(api));
		System.out.println(String.format("%-25s", "Custom-Properties: ") + getCustomProps(api));
	}
	
	private boolean hasQuota(API api) {
		return (api.getApplicationQuota()!=null) && 
				api.getApplicationQuota().getRestrictions()!=null &&
				api.getApplicationQuota().getRestrictions().size()>0 && 
				api.getSystemQuota()!=null && 
				api.getSystemQuota().getRestrictions()!=null &&
				api.getSystemQuota().getRestrictions().size()>0
				;
	}
	
	private String getState(API api) {
		try {
			return api.getState();
		} catch (AppException e) {
			LOG.error("Error getting API state");
			return "Err";
		}
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
		try {
			return api.getPath();
		} catch (AppException e) {
			LOG.error("Error getting API path");
			return "Err";
		}
	}
	
	private boolean hasTags(API api) {
		return (api.getTags()==null);
	}
	
	private String getTags(API api) {
		if(api.getTags()==null) return "None";
		Iterator<String> it = api.getTags().keySet().iterator();
		List<String> tags = new ArrayList<String>();
		while(it.hasNext()) {
			String tagGroup = it.next();
			String[] tagValues = api.getTags().get(tagGroup);
			tags.add(tagGroup + ": " + Arrays.toString(tagValues));
		}
		return tags.toString().replace("[", "").replace("]", "");
	}
	
	private String getCustomProps(API api) {
		if(api.getCustomProperties()==null) return "N/A";
		Iterator<String> it = api.getCustomProperties().keySet().iterator();
		List<String> props = new ArrayList<String>();
		while(it.hasNext()) {
			String property = it.next();
			String value = api.getCustomProperties().get(property);
			props.add(property + ": " + value);
		}
		return props.toString().replace("[", "").replace("]", "");
	}
	
	private String getOrgCount(API api) {
		try {
			if(api.getClientOrganizations()==null) return "N/A";
			return Integer.toString(api.getClientOrganizations().size());
		} catch (AppException e) {
			LOG.error("Error getting API client organization");
			return "Err";
		}
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
	
	private String getGrantedOrganizations(API api) {
		try {
			if(api.getClientOrganizations()==null) return "N/A";
			List<String> grantedOrgs = new ArrayList<String>();
			for(Organization org : api.getClientOrganizations()) {
				grantedOrgs.add(org.getName());
			}
			return grantedOrgs.toString().replace("[", "").replace("]", "");
		} catch (Exception e) {
			LOG.error("Error getting API client organization");
			return "Err";
		}
	}
	
	private String getUsedPolicies(API api) {
		return getUsedPolicies(api, false);
	}
	
	private String getUsedPolicies(API api, boolean detailed) {
		Iterator<OutboundProfile> it;
		try {
			it = api.getOutboundProfiles().values().iterator();
		} catch (AppException e) {
			LOG.error("Error getting policy information for API", e);
			return "Err";
		}
		String result = "";
		while(it.hasNext()) {
			if(!result.equals("")) result += ", ";
			OutboundProfile profile = it.next();
			if(profile.getRouteType().equals("proxy")) continue;
			if(profile.getRequestPolicy()!=null && profile.getRequestPolicy().getName()!=null) {
				if(detailed) {
					result += "Request: " + profile.getRequestPolicy().getName();
				} else {
					result += profile.getRequestPolicy().getName();
				}
			}
			if(profile.getRoutePolicy()!=null && profile.getRoutePolicy().getName()!=null) {
				if(detailed) {
					result += "Routing: " + profile.getRoutePolicy().getName();
				} else {
					result += profile.getRoutePolicy().getName();
				}
			}
			if(profile.getResponsePolicy()!=null && profile.getResponsePolicy().getName()!=null) {
				if(detailed) {
					result += "Response: " + profile.getResponsePolicy().getName();
				} else {
					result += profile.getResponsePolicy().getName();
				}
			}
			if(profile.getFaultHandlerPolicy()!=null && profile.getFaultHandlerPolicy().getName()!=null) {
				if(detailed) {
					result += "Fault-Handler: " + profile.getFaultHandlerPolicy().getName();
				} else {
					result += profile.getFaultHandlerPolicy().getName();
				}
			}
		}
		if(result.equals("")) return "None";
		return result;
	}
	
	private String getBackendPath(API api) {
		ExportAPI exportAPI = new ExportAPI(api);
		return exportAPI.getBackendBasepath();
	}

	private String getUsedSecurity(API api) {
		List<String> usedSecurity = new ArrayList<String>();
		Map<String, SecurityProfile> secProfilesMappedByName = new HashMap<String, SecurityProfile>();
		try {
			for(SecurityProfile secProfile : api.getSecurityProfiles()) {
				secProfilesMappedByName.put(secProfile.getName(), secProfile);
			}
		
			Iterator<InboundProfile> it;
			it = api.getInboundProfiles().values().iterator();

		while(it.hasNext()) {
			InboundProfile profile = it.next();
			SecurityProfile usedSecProfile = secProfilesMappedByName.get(profile.getSecurityProfile());
			for(SecurityDevice device : usedSecProfile.getDevices()) {
				usedSecurity.add(""+device.getType());
			}
		}
		String result = usedSecurity.toString().replace("[", "").replace("]", "");
		return result;
		} catch (AppException e) {
			LOG.error("Error getting security information for API", e);
			return "Err";
		}
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
