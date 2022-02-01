package com.axway.apim.api.apiSpecification;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public class WADLSpecification extends APISpecification {
	
	String wadl = null; 

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.WADL_API;
	}
	
	@Override
	public byte[] getApiSpecificationContent() {
		return this.apiSpecificationContent;
	}

	@Override
	public void configureBasepath(String backendBasepath, API api) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasepath!=null) {
				URL url = new URL(backendBasepath); // Parse it to make sure it is valid
				if(url.getPath()!=null && !url.getPath().equals("") && !backendBasepath.endsWith("/")) { // See issue #178
					backendBasepath += "/";
				}
				// The WADL has the base path configured like so: <resources base="http://customer-api.ddns.net:8099/">
				wadl = wadl.replaceFirst("(<resources.*base=\").*(\">)", "$1"+backendBasepath+"$2");

				this.apiSpecificationContent = wadl.getBytes();
			}
		} catch (MalformedURLException e) {
			throw new AppException("The configured backendBasepath: '"+backendBasepath+"' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	
	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		super.parse(apiSpecificationContent);
		if(apiSpecificationFile.toLowerCase().endsWith(".url")) {
			apiSpecificationFile = Utils.getAPIDefinitionUriFromFile(apiSpecificationFile);
		}
		if(!apiSpecificationFile.toLowerCase().endsWith(".wadl") && !new String(this.apiSpecificationContent, 0, 500).contains("wadl.dev.java.net")) {
			LOG.debug("No WADL specification. Specification doesn't contain WADL namespace: wadl.dev.java.net in the first 500 characters.");
			return false;
		}
			// We going to use a cheap way - Avoid parsing & writing back the WADL-File.
		this.wadl = new String(apiSpecificationContent);
		return true;
	}
}
