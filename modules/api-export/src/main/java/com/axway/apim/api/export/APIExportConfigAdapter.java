package com.axway.apim.api.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIMgrProxiesAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.IAPI;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.export.jackson.serializer.AIPQuotaSerializerModifier;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.model.APIImage;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIExportConfigAdapter {
	private static Logger LOG = LoggerFactory.getLogger(APIExportConfigAdapter.class);

	/** Which APIs should be exported identified by the path */
	private String exportApiPath = null;
	
	/** Is set if APIs only with that V-Host should be exported */
	private String exportVhost = null;

	/** Where to store the exported API-Definition */
	private String givenExportFolder = null;

	APIManagerAdapter apiManager;
	
	APIExportParams params;

	public APIExportConfigAdapter(String exportApiPath, String givenExportFolder, String exportVhost) throws AppException {
		super();
		this.exportApiPath = exportApiPath;
		this.exportVhost = (exportVhost!=null && !exportVhost.equals("NOT_SET")) ? exportVhost : null;
		this.givenExportFolder = (givenExportFolder==null) ? "." : givenExportFolder;
		LOG.debug("Constructed ExportConfigAdapter: [exportApiPath: '"+exportApiPath+"', givenExportFolder: '"+givenExportFolder+"', exportVhost: '"+exportVhost+"']");
		apiManager = APIManagerAdapter.getInstance();
		params = APIExportParams.getInstance();
	}

	public void exportAPIs() throws AppException {
		List<ExportAPI> exportAPIs = getAPIsToExport();
		for (ExportAPI exportAPI : exportAPIs) {
			try {
				saveAPILocally(exportAPI);
			} catch (AppException e) {
				LOG.error("Can't export API: " + e.getMessage() + " Please check in API-Manager UI the API is valid.", e);
			}
		}
	}

	private List<ExportAPI> getAPIsToExport() throws AppException {
		List<ExportAPI> exportAPIList = new ArrayList<ExportAPI>();
		if (!this.exportApiPath.contains("*")) { // Direct access with a specific API exposure path
			JsonNode mgrAPI = new APIMgrProxiesAdapter.Builder(APIManagerAdapter.TYPE_FRONT_END).hasApiPath(this.exportApiPath).hasVHost(exportVhost).build().getAPI(true);
			if(mgrAPI==null) {
				ErrorState.getInstance().setError("No API found for: '" + this.exportApiPath + "'", ErrorCode.UNKNOWN_API, false);
				throw new AppException("No API found for: '" + this.exportApiPath + "'", ErrorCode.UNKNOWN_API);
			}
			exportAPIList.add(getExportAPI(mgrAPI));
		} else if(APIManagerAdapter.hasAPIManagerVersion("7.7") // Wild-Card search on API-Manager >7.7 filtering directly
				&& (this.exportApiPath.startsWith("*") || this.exportApiPath.endsWith("*"))) {
			List<NameValuePair> filters = new ArrayList<NameValuePair>();
			filters.add(new BasicNameValuePair("field", "path"));
			filters.add(new BasicNameValuePair("op", "like"));
			if(exportApiPath.equals("*")) {
				LOG.info("Using '*' to export all APIs from API-Manager.");
				filters.add(new BasicNameValuePair("value", "/"));
			} else {
				LOG.info("Using wildcard pattern: '"+exportApiPath+"' to export APIs from API-Manager.");
				filters.add(new BasicNameValuePair("value", exportApiPath.replace("*", "")));
			}
			List<JsonNode> foundAPIs = new APIMgrProxiesAdapter.Builder(APIManagerAdapter.TYPE_FRONT_END).useFilter(filters).hasVHost(exportVhost).build().getAPIs(false);
			for(JsonNode mgrAPI : foundAPIs) {
				exportAPIList.add(getExportAPI(mgrAPI));
			}
		} else { // Get all APIs and filter them out manually
			Pattern pattern = Pattern.compile(exportApiPath.replace("*", ".*"));
			List<JsonNode> foundAPIs = new APIMgrProxiesAdapter.Builder(APIManagerAdapter.TYPE_FRONT_END).hasVHost(exportVhost).build().getAPIs(false);
			if(foundAPIs.size()>20) LOG.info("Loading actual API state from API-Manager. This may take a while. Please wait.\n");
			for(JsonNode mgrAPI : foundAPIs) {
				String apiPath = mgrAPI.get("path").asText();
				Matcher matcher = pattern.matcher(apiPath);
				if(matcher.matches()) {
					LOG.info("Adding API with path: '"+apiPath+"' based on requested path: '"+exportApiPath+"' to the export list.");
					exportAPIList.add(getExportAPI(mgrAPI));
				}
			}			
		}
		return exportAPIList;
	}
	
	private ExportAPI getExportAPI(JsonNode mgrAPI) throws AppException {
		IAPI actualAPI = apiManager.getAPIManagerAPI(mgrAPI, getAPITemplate(), API.class);
		handleCustomProperties(actualAPI);
		APIManagerAdapter.getInstance().translateMethodIds(actualAPI.getInboundProfiles(), actualAPI, true);
		APIManagerAdapter.getInstance().translateMethodIds(actualAPI.getOutboundProfiles(), actualAPI, true);
		return new ExportAPI(actualAPI);
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
			mapper.writeValue(new File(localFolder.getCanonicalPath() + "/api-config.json"), exportAPI);
		} catch (Exception e) {
			throw new AppException("Can't write API-Configuration file for API: '"+exportAPI.getName()+"' exposed on path: '"+exportAPI.getPath()+"'.", ErrorCode.UNXPECTED_ERROR, e);
		}
		APIImage image = exportAPI.getAPIImage();
		if(image!=null) {
			writeBytesToFile(image.getImageContent(), localFolder+File.separator + image.getBaseFilename()+image.getFileExtension());
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
		String orgVHost = apiManager.getOrg(exportAPI.getOrganizationId()).getVirtualHost();
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

	/**
	 * We need this template to enforce loading of all properties for the actual
	 * API, without that API-ManagerAdapter will skip not defined properties.
	 * 
	 * @return
	 * @throws AppException
	 */
	private IAPI getAPITemplate() throws AppException {
		IAPI apiTemplate = new API();
		apiTemplate.setState(IAPI.STATE_PUBLISHED);
		apiTemplate.setClientOrganizations(new ArrayList<String>());
		// Required to force loading of actual quota!
		apiTemplate.setApplicationQuota(new APIQuota());
		apiTemplate.setSystemQuota(new APIQuota());
		// Given a NOT-KNOWN organization to force the API-Manager Adapter to set the correct orgName in the actual API
		apiTemplate.setOrganizationId("NOT-KNOWN");
		return apiTemplate;
	}
	
	private void handleCustomProperties(IAPI actualAPI) throws AppException {
		JsonNode customPropconfig = APIManagerAdapter.getCustomPropertiesConfig().get("api");
		if(customPropconfig == null) return; // No custom properties configured
		Map<String, String> customProperties = new LinkedHashMap<String, String>();
		JsonNode actualApiConfig = actualAPI.getApiConfiguration();
		// Check if Custom-Properties are configured
		Iterator<String> customPropKeys = customPropconfig.fieldNames();
		while(customPropKeys.hasNext()) {
			String key = customPropKeys.next();
			if(actualApiConfig.has(key)) {
				JsonNode value = actualApiConfig.get(key);
				if(value==null) continue;
					customProperties.put(key, value.asText());
			}
		}
		if(customProperties.size()>0) {
			((API)actualAPI).setCustomProperties(customProperties);
		}
	}
	
	private void prepareToSave(ExportAPI exportAPI) throws AppException {
		// Clean-Up some internal fields in Outbound-Profiles
		if(exportAPI.getOutboundProfiles()==null) return;
		OutboundProfile profile = exportAPI.getOutboundProfiles().get("_default");
		profile.setApiId(null);
		profile.setApiMethodId(null);
	}
}
