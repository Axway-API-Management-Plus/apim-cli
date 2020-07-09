package com.axway.apim.api.definition;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.jackson.YAMLFactoryExt;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public abstract class APISpecification {
	
	public static enum APISpecType {
		SWAGGGER_API_12("Swagger 1.2", ".json"),
		SWAGGGER_API_12_YAML("Swagger 1.2 (YAML)", ".yaml"),
		SWAGGGER_API_20("Swagger 2.0", ".json"),
		SWAGGGER_API_20_YAML("Swagger 2.0 (YAML)", ".yaml"),
		OPEN_API_30("Open API 3.0", ".json"),
		OPEN_API_30_YAML("Open API 3.0 (YAML)", ".yaml"),
		WSDL_API ("WSDL", ".xml"),
		UNKNOWN ("Unknown", ".txt");
		
		String niceName;
		String fileExtension;
		
		public String getNiceName() {
			return niceName;
		}

		public String getFileExtension() {
			return fileExtension;
		}

		APISpecType(String niceName, String fileExtension) {
			this.niceName = niceName;
			this.fileExtension = fileExtension;
		}
	}
	
	static Logger LOG = LoggerFactory.getLogger(APISpecification.class);
	
	protected ObjectMapper mapper = null;
	
	protected String apiSpecificationFile = null;
	
	protected byte[] apiSpecificationContent = null;
	
	protected String backendBasepath;

	public APISpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super();
		this.apiSpecificationContent = apiSpecificationContent;
		this.backendBasepath = backendBasepath;
	}
	
	public APISpecification() {
		super();
	}

	public String getApiSpecificationFile() {
		return apiSpecificationFile;
	}

	public void setApiSpecificationFile(String apiSpecificationFile) {
		this.apiSpecificationFile = apiSpecificationFile;
	}

	public byte[] getApiSpecificationContent() {
		return apiSpecificationContent;
	}

	public void setApiSpecificationContent(byte[] apiSpecificationContent) {
		this.apiSpecificationContent = apiSpecificationContent;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APISpecification) {
			APISpecification otherSwagger = (APISpecification)other;
			boolean rc = (Arrays.hashCode(this.apiSpecificationContent)) == Arrays.hashCode(otherSwagger.getApiSpecificationContent()); 
			if(!rc) {
				LOG.info("Detected API-Definition-Filesizes: API-Manager: " + this.apiSpecificationContent.length + " vs. Import: " + otherSwagger.getApiSpecificationContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}
	
	protected abstract void configureBasepath() throws AppException;
	
	public abstract APISpecType getAPIDefinitionType() throws AppException;
	
	public abstract boolean configure() throws AppException;
	
	protected void setMapperForDataFormat() throws AppException {
		YAMLFactory yamlFactory = new YAMLFactoryExt().disable(Feature.WRITE_DOC_START_MARKER);
		JsonFactory jsonFactory = new JsonFactory();
		DataFormatDetector detector = new DataFormatDetector(yamlFactory, jsonFactory);
		DataFormatMatcher formatMatcher;
		try {
			formatMatcher = detector.findFormat(apiSpecificationContent);
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
				ErrorState.getInstance().setError("YAML API-Definition not supported by your API-Manager version", ErrorCode.UNSUPPORTED_FEATURE, false);
				throw new AppException("YAML API-Definition not supported by your API-Manager version", ErrorCode.UNSUPPORTED_FEATURE);
			}
			break;
		default:
			break;
		}
	}
}
