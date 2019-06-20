package com.axway.apim.swagger.api.properties;

import java.net.URL;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.Utils;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIDefintion {
	
	static Logger LOG = LoggerFactory.getLogger(APIDefintion.class);
	
	private String apiDefinitionFile = null;
	
	private byte[] apiDefinitionContent = null;

	public APIDefintion() {
		
	}

	public APIDefintion(byte[] apiDefinitionContent) {
		this.apiDefinitionContent = apiDefinitionContent;
	}

	public String getAPIDefinitionFile() {
		return apiDefinitionFile;
	}

	public void setAPIDefinitionFile(String apiDefinitionFile) {
		this.apiDefinitionFile = apiDefinitionFile;
	}

	public void setAPIDefinitionContent(byte[] apiDefinitionContent, DesiredAPI importAPI) {
		this.apiDefinitionContent = apiDefinitionContent;
		try {
			if(CommandParameters.getInstance().replaceHostInSwagger() && getAPIDefinitionType()==IAPI.SWAGGGER_API) {
				if(importAPI.getBackendBasepath()!=null) {
					URL url = new URL(importAPI.getBackendBasepath());
					int port = url.getPort()==-1 ? url.getDefaultPort() :  url.getPort();
					boolean noPortRequired = false;
					if(port==443 || port==80) noPortRequired = true; 
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode swagger = objectMapper.readTree(apiDefinitionContent);
					if(swagger.get("host")==null) {
						LOG.info("Adding new host '"+url.getHost()+":"+port+"' to Swagger-File based on configured backendBasepath: '"+importAPI.getBackendBasepath()+"'");
						((ObjectNode)swagger).put("host", url.getHost()+":"+port);
						this.apiDefinitionContent = objectMapper.writeValueAsBytes(swagger);
					} else {
						if(!swagger.get("host").asText().equals(url.getHost()+":"+port) && (noPortRequired && swagger.get("host").asText().equals(url.getHost()))) {
							LOG.info("Replacing existing host: '"+swagger.get("host").asText()+"' in Swagger-File to '"+url.getHost()+":"+port+"' based on configured backendBasepath: '"+importAPI.getBackendBasepath()+"'");
							((ObjectNode)swagger).put("host", url.getHost()+":"+port);
							this.apiDefinitionContent = objectMapper.writeValueAsBytes(swagger);
						} else {
							LOG.info("Swagger Host: '"+swagger.get("host").asText()+"' already matched configuredBackendHost: '"+importAPI.getBackendBasepath()+"'. Nothing to do.");
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
			
		}
	}

	public byte[] getAPIDefinitionContent() {
		return apiDefinitionContent;
	}
	
	public int getAPIDefinitionType() throws AppException {
		String apiDefinitionSource = null;
		if(this.apiDefinitionFile.toLowerCase().endsWith(".url")) {
			apiDefinitionSource = Utils.getAPIDefinitionUriFromFile(this.apiDefinitionFile);
		} else {
			apiDefinitionSource = this.apiDefinitionFile;
		}
		if(apiDefinitionSource.toLowerCase().endsWith("?wsdl")) {
			return IAPI.WSDL_API;
		} else {
			return IAPI.SWAGGGER_API;
		}
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIDefintion) {
			APIDefintion otherSwagger = (APIDefintion)other;
			boolean rc = (Arrays.hashCode(this.apiDefinitionContent)) == Arrays.hashCode(otherSwagger.getAPIDefinitionContent()); 
			if(!rc) {
				LOG.info("Detected API-Definition-Filesizes: API-Manager: " + this.apiDefinitionContent.length + " vs. Import: " + otherSwagger.getAPIDefinitionContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}

}
