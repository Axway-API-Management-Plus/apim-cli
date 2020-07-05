package com.axway.apim.organization.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.organization.lib.OrgExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonOrgExporter extends OrganizationExporter {

	public JsonOrgExporter(OrgExportParams params) {
		super(params);
	}

	@Override
	public void export(List<Organization> orgs) throws AppException {
		for(Organization org : orgs) {
			saveOrganizationLocally(org);
		}
	}
	
	private void saveOrganizationLocally(Organization org) throws AppException {
		String folderName = getExportFolder(org);
		String targetFolder = params.getTarget();
		File localFolder = new File(targetFolder +File.separator+ folderName);
		LOG.info("Going to export organizations into folder: " + localFolder);
		if(localFolder.exists()) {
			if(OrgExportParams.getInstance().deleteTarget()) {
				LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: " + localFolder + " already exists. Organization will not be exported. (You may set -deleteTarget)");
				this.hasError = true;
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		ObjectMapper mapper = new ObjectMapper();
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("OrganizationFilter",
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"id", "dn" }))
				.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(new String[] {}));
		mapper.setFilterProvider(filters);
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/org-config.json"), org);
		} catch (Exception e) {
			throw new AppException("Can't write configuration file for organization: '"+org.getName()+"'", ErrorCode.UNXPECTED_ERROR, e);
		}
		LOG.info("Successfully exported application to folder: " + localFolder);
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export is restricted to its own organization.");
		}
	}
	
	private String getExportFolder(Organization org) {
		String name = org.getName();
		name = name.replace(" ", "-");
		return name;
	}

	@Override
	public OrgFilter getFilter() throws AppException {
		return getBaseOrgFilterBuilder().build();
	}
}
