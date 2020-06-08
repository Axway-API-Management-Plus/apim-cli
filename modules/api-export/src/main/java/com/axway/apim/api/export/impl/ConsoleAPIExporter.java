package com.axway.apim.api.export.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder.Type;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleAPIExporter extends APIExporter {

	public ConsoleAPIExporter(APIExportParams params) {
		super(params);
	}

	@Override
	public void export(List<API> apis) throws AppException {
		System.out.println(AsciiTable.getTable(apis, Arrays.asList(
				new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getId()),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(api -> api.getName()),
				new Column().header("Version").with(api -> api.getVersion()),
				new Column().header("V-Host").with(api -> api.getVhost()),
				new Column().header("State").with(api -> getState(api)),
				new Column().header("Security").with(api -> getUsedSecurity(api)),
				new Column().header("Policies").with(api -> getUsedPolicies(api)),
				new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(api -> api.getOrganization().getName()),
				new Column().header("Orgs").with(api -> getOrgCount(api)),
				new Column().header("Apps").with(api -> Integer.toString(api.getApplications().size())),
				new Column().header("Quotas").with(api -> Boolean.toString(hasQuota(api))
				))));
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
	
	private String getOrgCount(API api) {
		try {
			return Integer.toString(api.getClientOrganizations().size());
		} catch (AppException e) {
			LOG.error("Error getting API client organization");
			return "Err";
		}
	}
	
	private String getUsedPolicies(API api) {
		List<String> policies = new ArrayList<String>();
		Iterator<OutboundProfile> it;
		try {
			it = api.getOutboundProfiles().values().iterator();
		} catch (AppException e) {
			LOG.error("Error getting policy information for API", e);
			return "Err";
		}
		while(it.hasNext()) {
			OutboundProfile profile = it.next();
			if(profile.getRouteType().equals("proxy")) continue;
			if(profile.getRequestPolicy()!=null && profile.getRequestPolicy().getName()!=null) policies.add(profile.getRequestPolicy().getName());
			if(profile.getRoutePolicy()!=null && profile.getRoutePolicy().getName()!=null) policies.add(profile.getRoutePolicy().getName());
			if(profile.getResponsePolicy()!=null && profile.getResponsePolicy().getName()!=null) policies.add(profile.getResponsePolicy().getName());
		}
		if(policies.size()==0) return "None";
		String result = policies.toString().replace("[", "").replace("]", "");
		return result;
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
		APIFilter filter = new APIFilter.Builder(Type.ACTUAL_API)
				.hasVHost(params.getValue("vhost"))
				.hasApiPath(params.getValue("api-path"))
				.hasId(params.getValue("id"))
				.hasName(params.getValue("name"))
				.hasState(params.getValue("state"))
				.includeQuotas(true)
				.includeImage(false)
				.includeOriginalAPIDefinition(false)
				.includeClientAppQuota(false)
				.build();
		return filter;
	}
}
