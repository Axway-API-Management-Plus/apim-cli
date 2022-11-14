package com.axway.apim.api.apiSpecification;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ODataSpecification extends APISpecification {
    private final Logger LOG = LoggerFactory.getLogger(ODataSpecification.class);
    protected OpenAPI openAPI;
    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        LOG.info("Overriding backend base path : {}", backendBasePath);
        if (backendBasePath == null || !CoreParameters.getInstance().isReplaceHostInSwagger()) {
            // Try to set up the Backend-Host + BasePath based on the given Metadata URL
            try {
                String backend = getBasePath(apiSpecificationFile);
                Server server = new Server();
                LOG.info("Set backend server: " + backend + " based on given Metadata URL");
                server.setUrl(backend);
                openAPI.addServersItem(server);
            } catch (MalformedURLException e) {
                String replaceHostInSwaggerDisabledNote = "";
                if (!CoreParameters.getInstance().isReplaceHostInSwagger()) {
                    replaceHostInSwaggerDisabledNote = " with parameter: replaceHostInSwagger set to true";
                }
                throw new AppException("Error importing OData API. Unknown backend host. "
                        + "You either have to provide the MetaData-File using an HTTP-Endpoint or configure a backendBasePath" + replaceHostInSwaggerDisabledNote + ".", ErrorCode.CANT_READ_API_DEFINITION_FILE);
            }
        } else {
            // Otherwise we are using the configured backendBasePath
            try {
                URL url = new URL(backendBasePath); // Parse it to make sure it is valid
                if (url.getPath() != null && !url.getPath().equals("") && !backendBasePath.endsWith("/")) { // See issue #178
                    backendBasePath += "/";
                }
                Server server = new Server();
                server.setUrl(backendBasePath);
                openAPI.addServersItem(server);

            } catch (MalformedURLException e) {
                throw new AppException("The configured backendBasePath: '" + backendBasePath + "' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
            } catch (Exception e) {
                LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
            }
        }
    }

    @Override
    public byte[] getApiSpecificationContent() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
        FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                SimpleBeanPropertyFilter.serializeAllExcept("exampleSetFlag"));
        mapper.setFilterProvider(filter);
        mapper.addMixIn(Object.class, OpenAPIMixIn.class);
        try {
            return mapper.writeValueAsBytes(openAPI);
        } catch (JsonProcessingException e) {
            LOG.error("Error creating OpenAPI specification based on OData specification", e);
            return null;
        }
    }

    private String getBasePath(String pathToMetaData) throws MalformedURLException {
        // Only if the MetaData-Description is given from an HTTP-Endpoint we can use it
        new URL(pathToMetaData); // Try to parse it, only to see if it's a valid URL
        pathToMetaData = pathToMetaData.substring(0, pathToMetaData.lastIndexOf("/"));
        return pathToMetaData;
    }

    protected ApiResponse createResponse(String description, Schema<?> schema) {
        ApiResponse response = new ApiResponse();
        response.setDescription(description);
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        response.setContent(content);
        return response;
    }

    @Override
    public String getDescription() {
        return openAPI.getInfo().getDescription();
    }

    protected Schema<?> getSimpleSchema(String type) {
        switch (type) {
            case "Guid":
                return new UUIDSchema();
            case "Int16":
            case "Int32":
            case "Int64":
            case "Decimal":
                return new IntegerSchema();
            case "Double":
                return new NumberSchema();
            case "String":
            case "Single":
            case "Time":
            case "DateTimeOffset":
            case "GeographyPoint":
            case "Duration":
                return new StringSchema();
            case "DateTime":
                return new DateTimeSchema();
            case "Binary":
                return new BinarySchema();
            case "Boolean":
                return new BooleanSchema();
        }
        return null;
    }
}
