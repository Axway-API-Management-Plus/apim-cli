package com.axway.apim.setup.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.adapter.jackson.UserSerializerModifier;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonAPIManagerSetupExporter extends APIManagerSetupResultHandler {

	public JsonAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) {
		super(params, result);
	}
	
	@Override
	public RemoteHostFilter getRemoteHostFilter() throws AppException {
		return getRemoteHostBaseFilterBuilder().build();
	}

	@Override
	public void export(APIManagerConfig apimanagerConfig) throws AppException {
		String folderName = getExportFolder(apimanagerConfig.getConfig());
		String targetFolder = params.getTarget();
		File localFolder = new File(targetFolder +File.separator+ folderName);
		LOG.info("Going to export API-Manager configuration into folder: " + localFolder);
		if(localFolder.exists()) {
			if(params.isDeleteTarget()) {
				LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: " + localFolder + " already exists. Configuration will not be exported. (You may set -deleteTarget)");
				this.hasError = true;
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.registerModule(new SimpleModule().setSerializerModifier(new PolicySerializerModifier(true)));
			mapper.registerModule(new SimpleModule().setSerializerModifier(new UserSerializerModifier(true)));
			FilterProvider filters = new SimpleFilterProvider()
					.addFilter("RemoteHostFilter", SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"id", "organizationId"}))
					.addFilter("APIManagerConfigFilter", SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"os", "architecture", "productVersion", "baseOAuth"}))
					.setFailOnUnknownId(false);
			mapper.setFilterProvider(filters);
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/apimanager-config.json"), apimanagerConfig);
			result.addExportedFile(localFolder.getCanonicalPath() + "/apimanager-config.json");
		} catch (Exception e) {
			throw new AppException("Can't create configuration export", ErrorCode.UNXPECTED_ERROR, e);
		}
		LOG.info("Successfully exported API-Manager configuration into: " + localFolder + File.separator + "apimanager-config.json");
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export of configuration restricted.");
		}
	}
	
	private String getExportFolder(Config config) {
		try {
			if(config==null) {
				config = APIManagerAdapter.getInstance().configAdapter.getConfig(APIManagerAdapter.hasAdminAccount());
			}
			String name = config.getPortalName().toLowerCase();
			name = name.replace(" ", "-");
			return name;
		} catch (Exception e) {
			LOG.warn("Error defining export folder. Error message: " + e.getMessage());
			if(LOG.isDebugEnabled()) {
				LOG.error("Error defining export folder.", e);
			}
			return "";
		}
	}
	
	public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
