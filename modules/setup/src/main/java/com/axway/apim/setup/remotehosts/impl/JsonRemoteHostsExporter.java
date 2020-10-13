package com.axway.apim.setup.remotehosts.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.adapter.jackson.UserSerializerModifier;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonRemoteHostsExporter extends RemoteHostsResultHandler {

	public JsonRemoteHostsExporter(RemoteHostsExportParams params) {
		super(params);
	}

	@Override
	public void export(List<RemoteHost> remoteHosts) throws AppException {
		for(RemoteHost remotehost : remoteHosts) {
			String folderName = getExportFolder(remotehost);
			String targetFolder = params.getTarget();
			File localFolder = new File(targetFolder +File.separator+ folderName);
			LOG.info("Going to export API-Manager Remote-Host into folder: " + localFolder);
			if(localFolder.exists()) {
				if(RemoteHostsExportParams.getInstance().isDeleteTarget()) {
					LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
					try {
						FileUtils.deleteDirectory(localFolder);
					} catch (IOException e) {
						throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
					}				
				} else {
					LOG.warn("Local export folder: " + localFolder + " already exists. Remote-Host will not be exported. (You may set -deleteTarget)");
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
				mapper.registerModule(new SimpleModule().setSerializerModifier(new UserSerializerModifier(true)));
				FilterProvider filters = new SimpleFilterProvider()
						.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"id"}));
				mapper.setFilterProvider(filters);
				mapper.writeValue(new File(localFolder.getCanonicalPath() + "/remote-host.json"), remotehost);
			} catch (Exception e) {
				throw new AppException("Can't create Remote-Host export", ErrorCode.UNXPECTED_ERROR, e);
			}
			LOG.info("Successfully exported API-Manager Remote-Host into folder: " + localFolder);
		}
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export of remote hosts is restricted.");
		}
	}
	
	private String getExportFolder(RemoteHost remoteHost) {
		String name = remoteHost.getName() + " " + remoteHost.getPort();
		name = name.replace(" ", "-");
		name = name.replace(".", "-");
		return name;
	}

	@Override
	public RemoteHostFilter getFilter() throws AppException {
		return getBaseFilterBuilder().build();
	}
	
	public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
