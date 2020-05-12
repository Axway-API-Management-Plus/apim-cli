package com.axway.apim.appexport.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.jackson.AppExportSerializerModifier;
import com.axway.apim.appexport.impl.jackson.ImageSerializer;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appexport.model.ExportApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonApplicationExporter extends ApplicationExporter {

	public JsonApplicationExporter(List<ClientApplication> apps, String folderToExport) {
		super(apps, folderToExport);
	}

	@Override
	public void export() throws AppException {
		for(ClientApplication app : this.apps) {
			saveApplicationLocally(new ExportApplication(app));
		}
	}
	
	private void saveApplicationLocally(ExportApplication app) throws AppException {
		String folderName = getExportFolder(app);
		File localFolder = new File(this.targetFolder +File.separator+ folderName);
		LOG.info("Going to export applications into folder: " + localFolder);
		if(localFolder.exists()) {
			if(AppExportParams.getInstance().deleteLocalFolder()) {
				LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: " + localFolder + " already exists. Application will not be exported. (You may set -df true)");
				this.hasError = true;
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().setSerializerModifier(new AppExportSerializerModifier(localFolder)));
		mapper.registerModule(new SimpleModule().addSerializer(Image.class, new ImageSerializer()));
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/"+app.getName()+".json"), app);
		} catch (Exception e) {
			throw new AppException("Can't write Application-Configuration file for application: '"+app.getName()+"'", ErrorCode.UNXPECTED_ERROR, e);
		}
		if(app.getImage()!=null) {
			writeBytesToFile(app.getImage().getImageContent(), localFolder+File.separator + app.getImage().getBaseFilename());
		}
		LOG.info("Successfully exported application to folder: " + localFolder);
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export is restricted by the following: ");
			LOG.warn("- No Quotas has been exported for the API");
			LOG.warn("- No Client-Organizations");
			LOG.warn("- Only subscribed applications from the Org-Admins organization");
		}
	}
	
	private String getExportFolder(ExportApplication app) {
		String appName = app.getName();
		appName = appName.replace(" ", "-");
		return appName;
	}

}
