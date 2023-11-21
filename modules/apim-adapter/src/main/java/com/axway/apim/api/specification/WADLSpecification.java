package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class WADLSpecification extends APISpecification {
    private static final Logger LOG = LoggerFactory.getLogger(WADLSpecification.class);

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
    public void updateBasePath(String basePath, String host) {
        // Not required
    }

    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        try {
            if (backendBasePath != null) {
                URL url = new URL(backendBasePath); // Parse it to make sure it is valid
                if (url.getPath() != null && !url.getPath().isEmpty() && !backendBasePath.endsWith("/")) { // See issue #178
                    backendBasePath += "/";
                }
                // The WADL has the base path configured like so: <resources base="http://customer-api.ddns.net:8099/">
                wadl = wadl.replaceFirst("(<resources.*base=\").*(\">)", "$1" + backendBasePath + "$2");//NOSONAR
                this.apiSpecificationContent = wadl.getBytes();
            }
        } catch (MalformedURLException e) {
            throw new AppException("The configured backendBasePath: '" + backendBasePath + "' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
        }
    }


    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        this.apiSpecificationContent = apiSpecificationContent;
        if (apiSpecificationFile.toLowerCase().endsWith(".url")) {
            apiSpecificationFile = Utils.getAPIDefinitionUriFromFile(apiSpecificationFile);
        }
        if (!apiSpecificationFile.toLowerCase().endsWith(".wadl") && !new String(this.apiSpecificationContent, 0, 500).contains("wadl.dev.java.net")) {
            LOG.debug("No WADL specification. Specification doesn't contain WADL namespace: wadl.dev.java.net in the first 500 characters.");
            return false;
        }
        // We are going to use a cheap way - Avoid parsing & writing back the WADL-File.
        this.wadl = new String(apiSpecificationContent);
        return true;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
