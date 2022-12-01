package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DATAPIExporter extends APIResultHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DATAPIExporter.class);

	/** Where to store the exported API-Definition */
	private final String givenExportFolder;
	
	private final String datPassword;

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
				throw e;
			}
		}
	}
	
	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}

	private void saveAPILocally(API api) throws AppException {
		String apiPath = getAPIExportFolder(api.getPath());
		File localFolder = new File(this.givenExportFolder +File.separator+ getVHost(api) + apiPath);
		LOG.debug("Going to export API: '"+api.toStringShort()+"' into folder: " + localFolder);
		validateFolder(localFolder);
		byte[] datFileContent = apiManager.apiAdapter.getAPIDatFile(api, datPassword);
		String targetFile = null;
		try {
			targetFile = localFolder.getCanonicalPath() + "/" + api.getName() + ".dat";
			writeBytesToFile(datFileContent, targetFile);
		} catch (IOException e) {
			throw new AppException("Can't save API-DAT file locally: " + targetFile,
					ErrorCode.UNXPECTED_ERROR, e);
		}
		LOG.info("Successfully exported API: "+api.toStringShort()+" as DAT-File into folder: " + localFolder.getAbsolutePath());
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
