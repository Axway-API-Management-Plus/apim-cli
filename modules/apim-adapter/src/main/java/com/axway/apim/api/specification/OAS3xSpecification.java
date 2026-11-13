package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.api.specification.filter.JsonNodeOpenAPI3SpecFilter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.InternalException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class OAS3xSpecification extends APISpecification {
    private static final Logger LOG = LoggerFactory.getLogger(OAS3xSpecification.class);
    public static final String SERVERS = "servers";
    public static final String OPENAPI = "openapi";

    private JsonNode openApiNode = null;
    private boolean resolveOpenApiRef = EnvironmentProperties.RESOLVE_OPENAPI_REF;

    public OAS3xSpecification() {
        super();
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        if (mapper.getFactory() instanceof YAMLFactory) {
            return APISpecType.OPEN_API_30_YAML;
        }
        return APISpecType.OPEN_API_30;
    }

    @Override
    public void filterAPISpecification() {
        if (filterConfig == null) return;
        JsonNodeOpenAPI3SpecFilter.filter(openApiNode, filterConfig);
    }

    @Override
    public String getDescription() {
        if (this.openApiNode.get("info") != null && this.openApiNode.get("info").get("description") != null) {
            return this.openApiNode.get("info").get("description").asText();
        } else {
            return "";
        }
    }

    @Override
    public byte[] getApiSpecificationContent() {
        // Return the original given API-Spec if no filters are applied
        if (this.filterConfig == null) return this.apiSpecificationContent;
        try {
            return mapper.writeValueAsBytes(openApiNode);
        } catch (JsonProcessingException e) {
            throw new InternalException("Error parsing API-Specification", e);
        }
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        try {
            String url = Utils.handleOpenAPIServerUrl(host, basePath);
            ObjectNode newServer = createObjectNode("url", url);
            ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(openApiNode);
        } catch (AppException e) {
            LOG.error("Cannot replace servers in openapi.", e);
        } catch (MalformedURLException e) {
            LOG.error("Unable to parse URL", e);
        } catch (JsonProcessingException e) {
            LOG.error("Cannot replace host in provided Open API. Continue with given host.", e);
        }
    }

    public ObjectNode createObjectNode(String key, String value) {
        ObjectNode newServer = this.mapper.createObjectNode();
        newServer.put(key, value);
        return newServer;
    }

    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        if (backendBasePath == null && !openApiNode.has(SERVERS)) {
            throw new AppException("The open API specification doesn't contain a servers section and no backend basePath is given", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
        try {
            if (openApiNode.has(SERVERS)) {
                ArrayNode servers = (ArrayNode) openApiNode.get(SERVERS);
                if (backendBasePath != null && !backendBasePath.contains("${env")) { // issue #332
                    JsonNode server = servers.get(0);
                    JsonNode urlJsonNode = server.get("url");
                    updateServer(urlJsonNode, backendBasePath);
                }
            } else {
                updateServer(backendBasePath);
            }
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(openApiNode);
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Open API. Continue with given host.", e);
        }
    }

    private void updateServer(JsonNode urlJsonNode, String backendBasePath) throws MalformedURLException {
        if (urlJsonNode != null) {
            String serverUrl = urlJsonNode.asText();
            if (CoreParameters.getInstance().isOverrideSpecBasePath()) {
                overrideServerSection(backendBasePath); // override openapi url with backendBaseapath
            } else if (!serverUrl.startsWith("http")) { // If url does not have hostname, add hostname from backendBasepath
                LOG.info("servers.url does not contain host name hence updating host value from backendBasepath : {}", backendBasePath);
                updateServerSection(backendBasePath, serverUrl);
            }
        }
    }

    private void updateServer(String backendBasePath) throws MalformedURLException {
        LOG.info("Server element not found");
        if (CoreParameters.getInstance().isOverrideSpecBasePath()) {
            if (backendBasePath != null)
                overrideServerSection(backendBasePath); // override openapi url to fix issue #412
        } else {
            LOG.info("Setting up server element as /");
            updateServerSection(backendBasePath, "/");
        }
    }

    public void updateServerSection(String backendBasePath, String serverUrl) throws MalformedURLException {
        String ignoreBasePath = Utils.ignoreBasePath(backendBasePath);
        backendBasePath = Utils.handleOpenAPIServerUrl(serverUrl, ignoreBasePath);
        LOG.info("Updating openapi Servers url with value : {}", backendBasePath);
        ObjectNode newServer = createObjectNode("url", backendBasePath);
        ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
    }

    public void overrideServerSection(String backendBasePath) {
        if (backendBasePath.endsWith("/"))
            backendBasePath = backendBasePath.substring(0, backendBasePath.length() - 1);
        LOG.info("overriding openapi Servers url with value : {}", backendBasePath);
        ObjectNode newServer = createObjectNode("url", backendBasePath);
        ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        try {
            this.apiSpecificationContent = apiSpecificationContent;
            setMapperForDataFormat();
            if (this.mapper == null) return false;
            openApiNode = this.mapper.readTree(apiSpecificationContent);
            LOG.debug("openapi tag value : {}", openApiNode.get(OPENAPI));
            if (!(openApiNode.has(OPENAPI) && openApiNode.get(OPENAPI).asText().startsWith("3."))) {
                return false;
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("No OpenAPI 3.x specification.", e);
            }
            return false;
        }
        // It is an OpenAPI 3.x specification. From here on, errors must not be swallowed, as the
        // specification would be reported as unsupported or the import would silently stop (issue #582)
        if (resolveOpenApiRef) {
            resolveReferences();
        }
        return true;
    }

    private void resolveReferences() throws AppException {
        try {
            this.apiSpecificationContent = resolveReferences(this.apiSpecificationContent);
            // Continue with the resolved specification
            this.openApiNode = this.mapper.readTree(this.apiSpecificationContent);
        } catch (Exception | LinkageError e) { // LinkageError: e.g. NoSuchMethodError caused by mixed up swagger library versions
            throw new AppException("Unable to resolve the references of the OpenAPI specification (resolve_openapi_ref=true): " + e, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
        }
    }

    public byte[] resolveReferences(byte[] apiSpecificationContent) throws IOException {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // Enable resolution of $ref references
        parseOptions.setFlatten(true);
        SwaggerParseResult swaggerParseResult = openAPIV3Parser.readContents(new String(apiSpecificationContent, StandardCharsets.UTF_8), null, parseOptions);
        List<String> messages = swaggerParseResult.getMessages();
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();
        if (openAPI == null) {
            throw new IOException("The OpenAPI specification cannot be parsed: " + messages);
        }
        if (messages != null) {
            for (String message : messages) {
                LOG.warn("Resolving OpenAPI references: {}", message);
            }
        }
        // Use the mappers of swagger-core to serialize the model. A plain Jackson mapper doesn't know how to
        // serialize the OpenAPI 3.1 model (e.g. boolean schemas like additionalProperties: false)
        boolean yaml = mapper.getFactory() instanceof YAMLFactory;
        boolean openApi31 = SpecVersion.V31.equals(openAPI.getSpecVersion());
        ObjectMapper swaggerMapper;
        if (yaml) {
            swaggerMapper = openApi31 ? Yaml31.mapper() : Yaml.mapper();
        } else {
            swaggerMapper = openApi31 ? Json31.mapper() : Json.mapper();
        }
        return swaggerMapper.writeValueAsBytes(openAPI);
    }

    void setResolveOpenApiRef(boolean resolveOpenApiRef) {
        this.resolveOpenApiRef = resolveOpenApiRef;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), openApiNode);
    }
}
