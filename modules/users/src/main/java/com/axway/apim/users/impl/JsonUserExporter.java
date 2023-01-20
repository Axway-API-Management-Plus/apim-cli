package com.axway.apim.users.impl;

import com.axway.apim.adapter.jackson.ImageSerializer;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.users.lib.ExportUser;
import com.axway.apim.users.lib.params.UserExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class JsonUserExporter extends UserResultHandler {

	public JsonUserExporter(UserExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public void export(List<User> users) throws AppException {
		for(User user : users) {
			saveUserLocally(new ExportUser(user));
		}
	}
	
	private void saveUserLocally(ExportUser user) throws AppException {
		String folderName = getExportFolder(user);
		String targetFolder = params.getTarget();
		File localFolder = new File(targetFolder +File.separator+ folderName);
		LOG.info("Going to export users into folder: {}", localFolder);
		if(localFolder.exists()) {
			if(UserExportParams.getInstance().isDeleteTarget()) {
				LOG.debug("Existing local export folder: {} already exists and will be deleted.", localFolder);
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: {} already exists. User will not be exported. (You may set -deleteTarget)", localFolder);
				this.hasError = true;
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().addSerializer(Image.class, new ImageSerializer()));
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("UserFilter",
						SimpleBeanPropertyFilter.serializeAllExcept("id", "dn"))
				.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept());
		mapper.setFilterProvider(filters);
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/user-config.json"), user);
			this.result.addExportedFile(localFolder.getCanonicalPath() + "/user-config.json");
		} catch (Exception e) {
			throw new AppException("Can't write configuration file for user: '"+user.getName()+"'", ErrorCode.UNXPECTED_ERROR, e);
		}
		if(user.getImage()!=null) {
			writeBytesToFile(user.getImage().getImageContent(), localFolder+File.separator + user.getImage().getBaseFilename());
		}
		LOG.info("Successfully exported user into folder: {}", localFolder);
	}
	
	private String getExportFolder(ExportUser user) {
		String loginName = user.getLoginName();
		loginName = loginName.replace(" ", "-");
		return loginName;
	}

	@Override
	public UserFilter getFilter() {
		return getBaseFilterBuilder().includeImage(true).build();
	}
	
	public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
