package com.axway.apim.setup.config.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.setup.config.ConfigExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonConfigExporter extends ConfigResultHandler {

	public JsonConfigExporter(ConfigExportParams params) {
		super(params);
	}

	@Override
	public void export(APIManagerConfig config) throws AppException {
		String folderName = getExportFolder(config);
		String targetFolder = params.getTarget();
		File localFolder = new File(targetFolder +File.separator+ folderName);
		LOG.info("Going to export API-Manager configuration into folder: " + localFolder);
		if(localFolder.exists()) {
			if(ConfigExportParams.getInstance().isDeleteTarget()) {
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
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/apimanager-config.json"), config);
		} catch (Exception e) {
			throw new AppException("Can't create configuration export", ErrorCode.UNXPECTED_ERROR, e);
		}
		LOG.info("Successfully exported API-Manager configuration into folder: " + localFolder);
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export of configuration restricted.");
		}
	}
	
	private String getExportFolder(APIManagerConfig config) {
		String name = config.getPortalName();
		name = name.replace(" ", "-");
		return name;
	}

	@Override
	public OrgFilter getFilter() throws AppException {
		return getBaseConfigFilterBuilder().build();
	}
	
	public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
