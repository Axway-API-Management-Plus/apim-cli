package com.axway.apim.api.export.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.jackson.JSONViews.APIForExport;
import com.axway.apim.api.API;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.jackson.serializer.AIPQuotaSerializerModifier;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonAPIExporter extends APIExporter {
	private static Logger LOG = LoggerFactory.getLogger(JsonAPIExporter.class);

	/** Where to store the exported API-Definition */
	private String givenExportFolder = null;

	APIManagerAdapter apiManager;
	
	public JsonAPIExporter(List<API> apis, APIExportParams params) throws AppException {
		super(apis, params);
		this.givenExportFolder = params.getLocalFolder();
	}
	
	@Override
	public void export() throws AppException {
		for (API api : this.apis) {
			ExportAPI exportAPI = new ExportAPI(api);
			try {
				saveAPILocally(exportAPI);
			} catch (AppException e) {
				LOG.error("Can't export API: " + e.getMessage() + " Please check in API-Manager UI the API is valid.", e);
			}
		}
		
	}

	private void saveAPILocally(ExportAPI exportAPI) throws AppException {
		String apiPath = getAPIExportFolder(exportAPI.getPath());
		File localFolder = new File(this.givenExportFolder +File.separator+ getVHost(exportAPI) + apiPath);
		LOG.info("Going to export API into folder: " + localFolder);
		if(localFolder.exists()) {
			if(params.deleteLocalFolder()) {
				LOG.debug("Existing local export folder: " + localFolder + " already exists and will be deleted.");
				try {
					FileUtils.deleteDirectory(localFolder);
				} catch (IOException e) {
					throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
				}				
			} else {
				LOG.warn("Local export folder: " + localFolder + " already exists. API will not be exported. (You may set -df true)");
				return;
			}
		}
		if (!localFolder.mkdirs()) {
			throw new AppException("Cant create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
		}
		APISpecification apiDef = exportAPI.getAPIDefinition();
		String targetFile = null;
		try {
			targetFile = localFolder.getCanonicalPath() + "/" + exportAPI.getName()+".json";
			writeBytesToFile(apiDef.getApiSpecificationContent(), targetFile);
			exportAPI.getAPIDefinition().setApiSpecificationFile(exportAPI.getName()+".json");
		} catch (IOException e) {
			throw new AppException("Can't save API-Definition locally to file: " + targetFile,
					ErrorCode.UNXPECTED_ERROR, e);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().setSerializerModifier(new AIPQuotaSerializerModifier()));
		mapper.setSerializationInclusion(Include.NON_NULL);
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("IgnoreImportFields",
						SimpleBeanPropertyFilter.filterOutAllExcept(new String[] {"inbound", "outbound", "certFile" }))
				.addFilter("IgnoreApplicationFields",
						SimpleBeanPropertyFilter.filterOutAllExcept(new String[] {"name", "oauthClientId", "extClientId", "apiKey" }));
		
		mapper.setFilterProvider(filters);
		try {
			prepareToSave(exportAPI);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writerWithView(APIForExport.class).writeValue(new File(localFolder.getCanonicalPath() + "/api-config.json"), exportAPI);
		} catch (Exception e) {
			throw new AppException("Can't write API-Configuration file for API: '"+exportAPI.getName()+"' exposed on path: '"+exportAPI.getPath()+"'.", ErrorCode.UNXPECTED_ERROR, e);
		}
		Image image = exportAPI.getAPIImage();
		if(image!=null) {
			writeBytesToFile(image.getImageContent(), localFolder+File.separator + image.getBaseFilename());
		}
		if(exportAPI.getCaCerts()!=null && !exportAPI.getCaCerts().isEmpty()) {
			storeCaCerts(localFolder, exportAPI.getCaCerts());
		}
		LOG.info("Successfully export API to folder: " + localFolder);
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export is restricted by the following: ");
			LOG.warn("- No Quotas has been exported for the API");
			LOG.warn("- No Client-Organizations");
			LOG.warn("- Only subscribed applications from the Org-Admins organization");
		}
	}
	
	private String getVHost(ExportAPI exportAPI) throws AppException {
		if(exportAPI.getVhost()!=null) return exportAPI.getVhost() + File.separator;
		String orgVHost = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder().hasId(exportAPI.getOrganizationId()).build()).getVirtualHost();
		if(orgVHost!=null) return orgVHost+File.separator;
		return "";
	}
	
	private void storeCaCerts(File localFolder, List<CaCert> caCerts) throws AppException {
		for(CaCert caCert : caCerts) {
			if (caCert.getCertBlob() == null) {
				LOG.warn("- Ignoring cert export for null certBlob for alias: {}", caCert.getAlias());
			} else {
				String filename = caCert.getCertFile();
				Base64.Encoder encoder = Base64.getMimeEncoder(64, System.getProperty("line.separator").getBytes());
				Base64.Decoder decoder = Base64.getDecoder();
				final String encodedCertText = new String(encoder.encode(decoder.decode(caCert.getCertBlob())));
				byte[] certContent = ("-----BEGIN CERTIFICATE-----\n" + encodedCertText + "\n-----END CERTIFICATE-----").getBytes();
				try {
					writeBytesToFile(certContent, localFolder + "/" + filename);
				} catch (AppException e) {
					throw new AppException("Can't write certificate to disc", ErrorCode.UNXPECTED_ERROR, e);
				}
			}
		}
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
	
	private void prepareToSave(ExportAPI exportAPI) throws AppException {
		// Clean-Up some internal fields in Outbound-Profiles
		if(exportAPI.getOutboundProfiles()==null) return;
		OutboundProfile profile = exportAPI.getOutboundProfiles().get("_default");
		profile.setApiId(null);
		profile.setApiMethodId(null);
	}
}
