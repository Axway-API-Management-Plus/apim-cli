package com.axway.apim.api.apiSpecification;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.jackson.YAMLFactoryExt;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APISpecificationFilter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class APISpecification {
	private final Logger LOG = LoggerFactory.getLogger(APISpecification.class);

	private byte[] originalApiSpecificationContent;

	public enum APISpecType {
		SWAGGER_API_1x("Swagger 1.x", ".json"),
		SWAGGER_API_1x_YAML("Swagger 1.x (YAML)", ".yaml"),
		SWAGGER_API_20("Swagger 2.0", ".json"),
		SWAGGER_API_20_YAML("Swagger 2.0 (YAML)", ".yaml"),
		OPEN_API_30("Open API 3.0", ".json"),
		OPEN_API_30_YAML("Open API 3.0 (YAML)", ".yaml"),
		WSDL_API ("WSDL", ".xml"),
		WADL_API ("Web Application Description Language (WADL)", ".wadl"),
		ODATA_V2 ("OData V2 (converted to OpenAPI 3.0.1)", "$metadata", "Given OData specification is converted into an OpenAPI 3 specification.", 
				"Please note: You need to use the OData-Routing policy for this API. See: https://github.com/Axway-API-Management-Plus/odata-routing-policy"),
		ODATA_V3 ("OData V4", "$metadata"),
		ODATA_V4 ("OData V4", "$metadata"),
		UNKNOWN ("Unknown", ".txt");
		
		final String niceName;
		final String fileExtension;
		final String note;
		final String additionalNote;
		
		public String getNiceName() {
			return niceName;
		}

		public String getFileExtension() {
			return fileExtension;
		}
		
		public String getNote() {
			if(note==null) return "";
			return note;
		}
		
		public String getAdditionalNote() {
			return additionalNote;
		}

		APISpecType(String niceName, String fileExtension) {
			this(niceName, fileExtension, null, null);
		}

		APISpecType(String niceName, String fileExtension, String note, String additionalNote) {
			this.niceName = niceName;
			this.fileExtension = fileExtension;
			this.note = note;
			this.additionalNote = additionalNote;
		}
	}
	protected ObjectMapper mapper = null;
	
	protected String apiSpecificationFile = null;
	
	protected byte[] apiSpecificationContent = null;
	
	protected APISpecificationFilter filterConfig = null;

	public String getApiSpecificationFile() {
		return apiSpecificationFile;
	}

	public void setApiSpecificationFile(String apiSpecificationFile) {
		this.apiSpecificationFile = apiSpecificationFile;
	}

	public abstract byte[] getApiSpecificationContent();
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APISpecification) {
			APISpecification otherSwagger = (APISpecification)other;
//			if(other instanceof Swagger2xSpecification){
//				new OpenAPIParse
//			}else if(other instanceof OAS3xSpecification){
//
//			}
			boolean rc = new String(originalApiSpecificationContent, StandardCharsets.UTF_8).contentEquals(new String(otherSwagger.getOriginalApiSpecificationContent(), StandardCharsets.UTF_8));
			if(!rc) {
				LOG.info("Detected API-Definition-File sizes: API-Manager: " + this.originalApiSpecificationContent.length + " vs. Import: " + otherSwagger.getOriginalApiSpecificationContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}
	
	public abstract void configureBasePath(String backendBasePath, API api) throws AppException;
	
	public abstract String getDescription();


	
	public abstract APISpecType getAPIDefinitionType() throws AppException;
	
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		this.apiSpecificationContent = apiSpecificationContent;
		this.originalApiSpecificationContent = apiSpecificationContent;
		return true;
	}
	
	protected void setMapperForDataFormat() throws AppException {
		YAMLFactory yamlFactory = new YAMLFactoryExt().disable(Feature.WRITE_DOC_START_MARKER);
		JsonFactory jsonFactory = new JsonFactory();
		DataFormatDetector detector = new DataFormatDetector(yamlFactory, jsonFactory);
		DataFormatMatcher formatMatcher;
		try {
			formatMatcher = detector.findFormat(this.apiSpecificationContent);
		} catch (IOException e) {
			LOG.error("Error detecting dataformat", e);
			return;
		}
	    if (formatMatcher.getMatchStrength() == MatchStrength.INCONCLUSIVE ||
	            formatMatcher.getMatchStrength() == MatchStrength.NO_MATCH) {
	    	this.mapper = new ObjectMapper();
	    }
		switch (formatMatcher.getMatchedFormatName().toLowerCase()) {
		case "json":
			this.mapper = new ObjectMapper(jsonFactory);
			LOG.trace("JSON API-Definition detected");
			break;
		case "yaml":
			this.mapper = new ObjectMapper(yamlFactory);
			LOG.trace("YAML API-Definition detected");
			if(!APIManagerAdapter.hasAPIManagerVersion("7.7")) {
				throw new AppException("YAML API-Definition not supported by your API-Manager version", ErrorCode.UNSUPPORTED_FEATURE);
			}
			break;
		default:
			break;
		}
	}

	public byte[] getOriginalApiSpecificationContent() {
		return originalApiSpecificationContent;
	}

	public void filterAPISpecification() {
	}

	public APISpecification setFilterConfig(APISpecificationFilter filterConfig) {
		this.filterConfig = filterConfig;
		return this;
	}
}
