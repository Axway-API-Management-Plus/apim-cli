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
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.jackson.serializer.APIExportSerializerModifier;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Image;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonAPIExporter extends APIResultHandler {
	private static Logger LOG = LoggerFactory.getLogger(JsonAPIExporter.class);

	/** Where to store the exported API-Definition */
	private String givenExportFolder = null;

	APIManagerAdapter apiManager;
	
	public JsonAPIExporter(APIExportParams params) throws AppException {
		super(params);
		this.givenExportFolder = params.getTarget();
	}
	
	@Override
	public void execute(List<API> apis) throws AppException {
		for (API api : apis) {
			ExportAPI exportAPI = new ExportAPI(api);
			try {
				saveAPILocally(exportAPI);
			} catch (AppException e) {
				LOG.error("Can't export API: " + e.getMessage() + " Please check in API-Manager UI the API is valid.", e);
			}
		}
		return;
	}
	
	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder()
				.includeQuotas(true)
				.includeImage(true)
				.includeClientApplications(true)
				.includeClientOrganizations(true)
				.includeOriginalAPIDefinition(true)
				.includeRemoteHost(true);
		return builder.build();
	}

	private void saveAPILocally(ExportAPI exportAPI) throws AppException {
		String apiPath = getAPIExportFolder(exportAPI.getPath());
		File localFolder = new File(this.givenExportFolder +File.separator+ getVHost(exportAPI) + apiPath);
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
		APISpecification apiDef = exportAPI.getAPIDefinition();
		String targetFile = null;
		try {
			targetFile = localFolder.getCanonicalPath() + "/" + exportAPI.getName()+apiDef.getAPIDefinitionType().getFileExtension();
			writeBytesToFile(apiDef.getApiSpecificationContent(), targetFile);
			exportAPI.getAPIDefinition().setApiSpecificationFile(exportAPI.getName()+apiDef.getAPIDefinitionType().getFileExtension());
		} catch (IOException e) {
			throw new AppException("Can't save API-Definition locally to file: " + targetFile,
					ErrorCode.UNXPECTED_ERROR, e);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().setSerializerModifier(new APIExportSerializerModifier()));
		//mapper.registerModule(new SimpleModule().addSerializer(new PolicyToNameSerializer()));
		mapper.setSerializationInclusion(Include.NON_NULL);
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("CaCertFilter",
						SimpleBeanPropertyFilter.filterOutAllExcept(new String[] {"inbound", "outbound", "certFile" }))
				.addFilter("ProfileFilter",
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"apiMethodId" }))
				.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(new String[] {}));
		mapper.setFilterProvider(filters);
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/api-config.json"), exportAPI);
		} catch (Exception e) {
			throw new AppException("Can't create API-Configuration file for API: '"+exportAPI.getName()+"' exposed on path: '"+exportAPI.getPath()+"'.", ErrorCode.UNXPECTED_ERROR, e);
		}
		Image image = exportAPI.getAPIImage();
		if(image!=null) {
			writeBytesToFile(image.getImageContent(), localFolder+File.separator + image.getBaseFilename());
		}
		if(exportAPI.getCaCerts()!=null && !exportAPI.getCaCerts().isEmpty()) {
			storeCaCerts(localFolder, exportAPI.getCaCerts());
		}
		LOG.info("Successfully exported API into folder: " + localFolder.getAbsolutePath());
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Export has been done with an Org-Admin account only. Export is restricted by the following: ");
			LOG.warn("- No Quotas has been exported for the API");
			LOG.warn("- No Client-Organizations");
			LOG.warn("- Only subscribed applications from the Org-Admins organization");
		}
	}
	
	private String getVHost(ExportAPI exportAPI) throws AppException {
		if(exportAPI.getVhost()!=null) return exportAPI.getVhost().replace(":", "_") + File.separator;
		String orgVHost = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder().hasId(exportAPI.getOrganizationId()).build()).getVirtualHost();
		if(orgVHost!=null) return orgVHost.replace(":", "_")+File.separator;
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
}
