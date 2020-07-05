package com.axway.apim.organization.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONOrgAdapter extends OrgAdapter {
	
	private ObjectMapper mapper = new ObjectMapper();

	public JSONOrgAdapter() {
	}

	public boolean readConfig(Object config) throws AppException {
		if (config==null) return false;
		if (config instanceof String == false) return false;
		String myConfig = (String)config;
		File configFile = new File(myConfig);
		if(!configFile.exists()) return false;
		try {
			this.orgs = mapper.readValue(configFile, new TypeReference<List<Organization>>(){});
		} catch (MismatchedInputException me) {
			try {
				Organization org = mapper.readValue(configFile, Organization.class);
				this.orgs = new ArrayList<Organization>();
				this.orgs.add(org);
			} catch (Exception pe) {
				throw new AppException("Cannot read organization(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read organization(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		return true;
	}
}
