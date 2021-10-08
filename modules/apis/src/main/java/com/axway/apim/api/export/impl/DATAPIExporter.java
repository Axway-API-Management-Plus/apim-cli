package com.axway.apim.api.export.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class DATAPIExporter extends APIResultHandler {
	private static Logger LOG = LoggerFactory.getLogger(DATAPIExporter.class);

	/** Where to store the exported API-Definition */
	private String givenExportFolder = null;
	
	private String datPassword = null;

	APIManagerAdapter apiManager = APIManagerAdapter.getInstance();
	
	public DATAPIExporter(APIExportParams params) throws AppException {
		super(params);
		this.givenExportFolder = params.getTarget();
		this.datPassword = params.getDatPassword();
	}
	
	@Override
	public void execute(List<API> apis) throws AppException {
		for (API api : apis) {
			try {
				saveAPILocally(api);
			} catch (AppException e) {
				LOG.error("Can't export API: " + e.getMessage() + " as DAT-File. Please check in API-Manager UI the API is valid.", e);
			}
		}
		return;
	}
	
	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}

	private void saveAPILocally(API api) throws AppException {
		String apiPath = getAPIExportFolder(api.getPath());
		File localFolder = new File(this.givenExportFolder +File.separator+ getVHost(api) + apiPath);
		LOG.info("Going to export API into folder: " + localFolder);
		if(localFolder.exists()) {
			if(params.isDeleteTarget()) {
				LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: " + localFolder + " already exists. API will not be exported. (You may set -deleteTarget)");
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cant create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		byte[] datFileContent = apiManager.apiAdapter.getAPIDatFile(api, datPassword);

		String targetFile = null;
		try {
			targetFile = localFolder.getCanonicalPath() + "/" + api.getName() + ".dat";
			writeBytesToFile(datFileContent, targetFile);
		} catch (IOException e) {
			throw new AppException("Can't save API-DAT file locally: " + targetFile,
					ErrorCode.UNXPECTED_ERROR, e);
		}
		LOG.info("Successfully exported API as DAT-File into folder: " + localFolder.getAbsolutePath());
	}
	
	private String getVHost(API api) throws AppException {
		if(api.getVhost()!=null) return api.getVhost() + File.separator;
		String orgVHost = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder().hasId(api.getOrganizationId()).build()).getVirtualHost();
		if(orgVHost!=null) return orgVHost+File.separator;
		return "";
	}
	

	private static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	private String getAPIExportFolder(String apiExposurePath) {
		if (apiExposurePath.startsWith("/"))
			apiExposurePath = apiExposurePath.replaceFirst("/", "");
		if (apiExposurePath.endsWith("/"))
			apiExposurePath = apiExposurePath.substring(0, apiExposurePath.length() - 1);
		apiExposurePath = apiExposurePath.replace("/", "-");
		return apiExposurePath;
	}
}
