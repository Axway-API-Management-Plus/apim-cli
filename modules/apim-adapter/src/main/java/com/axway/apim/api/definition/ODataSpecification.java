package com.axway.apim.api.definition;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

public abstract class ODataSpecification extends APISpecification {
	
	public ODataSpecification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	protected OpenAPI openAPI;
	
	@Override
	public void configureBasepath(String backendBasepath) throws AppException {
		if(backendBasepath==null || !CoreParameters.getInstance().isReplaceHostInSwagger()) {
			// Try to setup the Backend-Host + Basepath based on the given Metadata URL
			try {
				String backend = getBasePath(apiSpecificationFile);
				Server server = new Server();
				LOG.info("Set backend server: "+backend+" based on given Metadata URL");
				server.setUrl(backend);
				openAPI.addServersItem(server);
				return;
			} catch (MalformedURLException e) {
				String replaceHostInSwaggerDisabledNote = "";
				if(!CoreParameters.getInstance().isReplaceHostInSwagger()) {
					replaceHostInSwaggerDisabledNote = " with parameter: replaceHostInSwagger set to true";
				}
				throw new AppException("Error importing OData API. Unknown backend host. "
						+ "You either have to provide the MetaData-File using an HTTP-Endpoint or configure a backendBasepath"+replaceHostInSwaggerDisabledNote+".", ErrorCode.CANT_READ_API_DEFINITION_FILE);
			}
		}
		
		// Otherwise we are using the configured backendBasePath
		try {
			if(backendBasepath!=null) {
				URL url = new URL(backendBasepath); // Parse it to make sure it is valid
				if(url.getPath()!=null && !url.getPath().equals("") && !backendBasepath.endsWith("/")) { // See issue #178
					backendBasepath += "/";
				}
				Server server = new Server();
				server.setUrl(backendBasepath);
				openAPI.addServersItem(server);
			}
		} catch (MalformedURLException e) {
			throw new AppException("The configured backendBasepath: '"+backendBasepath+"' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	@Override
	public byte[] getApiSpecificationContent() {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
				SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"exampleSetFlag"}));
		mapper.setFilterProvider(filter);
		mapper.addMixIn(Object.class, OpenAPIMixIn.class);
		try {
			return mapper.writeValueAsBytes(openAPI);
		} catch (JsonProcessingException e) {
			LOG.error("Error creating OpenAPI specification based on ODataV2 Metadata", e);
			return null;
		}
	}
	
	private String getBasePath(String pathToMetaData) throws MalformedURLException {
		// Only if the MetaData-Description is given from an HTTP-Endpoint we can use it
		new URL(pathToMetaData); // Try to parse it, only to see if it's a valid URL
		pathToMetaData = pathToMetaData.substring(0, pathToMetaData.lastIndexOf("/"));
		return pathToMetaData;
	}

}
