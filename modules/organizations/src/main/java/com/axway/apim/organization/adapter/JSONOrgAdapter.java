package com.axway.apim.organization.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.lib.OrgImportParams;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONOrgAdapter extends OrgAdapter {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	OrgImportParams importParams;

	public JSONOrgAdapter(OrgImportParams params) {
		this.importParams = params;
	}

	public void readConfig() throws AppException {
		String config = importParams.getConfig();
		String stage = importParams.getStage();

		File configFile = Utils.locateConfigFile(config);
		if(!configFile.exists()) return;
		File stageConfig = Utils.getStageConfig(stage, configFile);
		List<Organization> baseOrgs;
		// Try to read a list of organizations
		try {
			baseOrgs = mapper.readValue(Utils.substitueVariables(configFile), new TypeReference<List<Organization>>(){});
			if(stageConfig!=null) {
				ErrorState.getInstance().setError("Stage overrides are not supported for organization lists.", ErrorCode.CANT_READ_CONFIG_FILE, false);
				throw new AppException("Stage overrides are not supported for organization lists.", ErrorCode.CANT_READ_CONFIG_FILE);
			} else {
				this.orgs = baseOrgs;
			}
		// Try to read single organization
		} catch (MismatchedInputException me) {
			try {
				Organization org = mapper.readValue(Utils.substitueVariables(configFile), Organization.class);
				if(stageConfig!=null) {
					try {
						ObjectReader updater = mapper.readerForUpdating(org);
						org = updater.readValue(Utils.substitueVariables(stageConfig));
					} catch (FileNotFoundException e) {
						LOG.warn("No config file found for stage: '"+stage+"'");
					}
				}
				this.orgs = new ArrayList<Organization>();
				this.orgs.add(org);
			} catch (Exception pe) {
				throw new AppException("Cannot read organization(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read organization(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		addImage(orgs, configFile.getParentFile());
		validateCustomProperties(orgs);
		return;
	}
	
	private void addImage(List<Organization> orgs, File parentFolder) throws AppException {
		for(Organization org : orgs) {
			if(org.getImageUrl()==null || org.getImageUrl().equals("")) continue;
			org.setImage(Image.createImageFromFile(new File(parentFolder + File.separator + org.getImageUrl())));
		}
	}
	
	private void validateCustomProperties(List<Organization> orgs) throws AppException {
		for(Organization org : orgs) {
			Utils.validateCustomProperties(org.getCustomProperties(), Type.organization);
		}
	}
}
