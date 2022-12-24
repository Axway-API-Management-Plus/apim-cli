package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.apiSpecification.WSDLSpecification;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.jackson.serializer.APIExportSerializerModifier;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Image;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class JsonAPIExporter extends APIResultHandler {
	private static final Logger LOG = LoggerFactory.getLogger(JsonAPIExporter.class);

	/** Where to store the exported API-Definition */
	private final String givenExportFolder;
	private final boolean exportMethods;
	
	public JsonAPIExporter(APIExportParams params) {
		super(params);
		this.givenExportFolder = params.getTarget();
		this.exportMethods = params.isExportMethods();
	}
	
	@Override
	public void execute(List<API> apis) throws AppException {
		for (API api : apis) {
			ExportAPI exportAPI = new ExportAPI(api);
			try {
				saveAPILocally(exportAPI);
			} catch (AppException e) {
				throw e;
			}
		}
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
		if(exportMethods)
			builder.includeMethods(true);
		return builder.build();
	}

	private void saveAPILocally(ExportAPI exportAPI) throws AppException {

		String apiPath = getAPIExportFolder(exportAPI.getPath());
		File localFolder = new File(this.givenExportFolder +File.separator+ getVHost(exportAPI) + apiPath);
		LOG.debug("Going to export API: {} into folder: {} ", exportAPI.toStringShort(), localFolder);
		validateFolder(localFolder);
		APISpecification apiDef = exportAPI.getAPIDefinition();
		// Skip processing if API definition is not available due to original API cloned and deleted.
		if(apiDef == null){
			LOG.error("Backend API Definition is not available for the API : {}, hence use the option -useFEAPIDefinition to export API", exportAPI.getName());
			return;
		}
		String targetFile = null;
		try {
			targetFile = localFolder.getCanonicalPath() + "/" + exportAPI.getName()+apiDef.getAPIDefinitionType().getFileExtension();
			if(!(apiDef instanceof WSDLSpecification && EnvironmentProperties.RETAIN_BACKED_URL)) {
				writeBytesToFile(apiDef.getApiSpecificationContent(), targetFile);
				exportAPI.getAPIDefinition().setApiSpecificationFile(exportAPI.getName() + apiDef.getAPIDefinitionType().getFileExtension());
			}
		} catch (IOException e) {
			throw new AppException("Can't save API-Definition locally to file: " + targetFile,
					ErrorCode.UNXPECTED_ERROR, e);
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().setSerializerModifier(new APIExportSerializerModifier()));
		mapper.setSerializationInclusion(Include.NON_NULL);
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("CaCertFilter",
						SimpleBeanPropertyFilter.filterOutAllExcept("inbound", "outbound", "certFile"))
				.addFilter("ProfileFilter",
						SimpleBeanPropertyFilter.serializeAllExcept("apiMethodId"))
				.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept());
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
		LOG.info("Successfully exported API: {} into folder: {}",exportAPI.toStringShort() , localFolder.getAbsolutePath());
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

		try (FileOutputStream fileOutputStream = new FileOutputStream(fileDest)) {
			fileOutputStream.write(bFile);
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
