package com.axway.apim.api.specification;

import com.axway.apim.api.specification.filter.OpenAPI3SpecificationFilter;
import com.axway.apim.lib.error.AppException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;

public class ODataV4Specification extends ODataSpecification {

    public static final String QUERY = "query";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String ENTITY_SET = "EntitySet ";
    private final Logger logger = LoggerFactory.getLogger(ODataV4Specification.class);
    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, String> knownEntityTags = new HashMap<>();
    private final Map<String, String> namespaceAliasMap = new HashMap<>();
    private final Map<FullQualifiedName, EdmAnnotations> entityAnnotations = new HashMap<>();

    @Override
    public void updateBasePath(String basePath, String host) { // implementation ignored
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        return APISpecType.ODATA_V4;
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) {
        try {
            this.apiSpecificationContent = apiSpecificationContent;
            ODataClient client = ODataClientFactory.getClient();
            Edm edm = client.getReader().readMetadata(new ByteArrayInputStream(apiSpecificationContent));
            this.openAPI = new OpenAPI();
            Info info = new Info();
            info.setTitle("OData Service");
            info.setDescription("The OData Service from " + apiSpecificationFile);
            // When running as part of an Integration-Test - This avoids creating a dynamic API-Specification file
            if (apiSpecificationFile.contains("ImportActionTest")) {
                info.setDescription("The OData Service from my test file");
            }
            info.setVersion("4.0");
            openAPI.setInfo(info);
            Components comp = new Components();
            this.openAPI.setComponents(comp);
            addTopSkipSearchAndCount(openAPI);
            addErrorSchema();
            addErrorResponse(openAPI);
            List<EdmSchema> edmSchemas = edm.getSchemas();
            for (EdmSchema schema : edmSchemas) {
                info.setTitle(schema.getNamespace() + " OData Service");
                namespaceAliasMap.put(schema.getNamespace(), schema.getAlias());
                for (EdmAnnotations annotationGroup : schema.getAnnotationGroups()) {
                    logger.info("Target path : {}", annotationGroup.getTargetPath());
                    entityAnnotations.put(new FullQualifiedName(annotationGroup.getTargetPath()), annotationGroup);
                }
                if (schema.getEntityContainer() != null) {
                    for (EdmEntitySet entityType : schema.getEntityContainer().getEntitySets()) {
                        openAPI.path(getEntityPath(entityType), getPathItemForEntity(edm, entityType, false));
                        openAPI.path(getEntityIdPath(entityType), getPathItemForEntity(edm, entityType, true));
                    }
                    for (EdmSingleton edmSingleton : schema.getEntityContainer().getSingletons()) {
                        openAPI.path(getSingletonPath(edmSingleton), getPathItemForEntity(edm, edmSingleton, false));
                        openAPI.path(getSingletonIdPath(edmSingleton), getPathItemForEntity(edm, edmSingleton, true));
                    }
                }
                for (EdmFunction function : schema.getFunctions()) {
                    openAPI.path("/" + function.getName(), getPathItemForFunction(edm, function));
                }
                for (EdmAction action : schema.getActions()) {
                    openAPI.path("/" + action.getName(), getPathItemForFunction(edm, action));
                }
            }
            comp.setSchemas(schemas);
            List<Tag> tags = getTags();
            this.openAPI.setTags(tags);
            createBatchResource(openAPI);
            return true;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error("Error parsing OData V4 MetaData.", e);
            }
            return false;
        }
    }

    public void addTopSkipSearchAndCount(OpenAPI openAPI) {
        Parameter topParameter = new Parameter();
        topParameter.setName("$top");
        topParameter.setIn(QUERY);
        IntegerSchema integerSchema = new IntegerSchema();
        integerSchema.setMinimum(BigDecimal.valueOf(0));
        integerSchema.setFormat(null);
        topParameter.setSchema(integerSchema);
        topParameter.setDescription("Show only the first n items, see [Paging - Top](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptiontop)");
        topParameter.setExample(50);
        Map<String, Parameter> parameters = openAPI.getComponents().getParameters();
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put("top", topParameter);
        Parameter skipParameter = new Parameter();
        skipParameter.setName("$skip");
        skipParameter.setIn(QUERY);
        skipParameter.setSchema(integerSchema);
        skipParameter.setDescription("Skip the first n items, see [Paging - Skip](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionskip)");
        parameters.put("skip", skipParameter);
        Parameter countParameter = new Parameter();
        countParameter.setName("$count");
        countParameter.setIn(QUERY);
        BooleanSchema booleanSchema = new BooleanSchema();
        countParameter.setSchema(booleanSchema);
        countParameter.setDescription("Include count of items, see [Count](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptioncount)");
        parameters.put("count", countParameter);
        Parameter searchParameter = new Parameter();
        searchParameter.setName("$search");
        searchParameter.setIn(QUERY);
        StringSchema stringSchema = new StringSchema();
        searchParameter.setSchema(stringSchema);
        searchParameter.setDescription("Search items by search phrases, see [Searching](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionsearch)");
        parameters.put("search", searchParameter);
        openAPI.getComponents().setParameters(parameters);
    }

    public void addErrorResponse(OpenAPI openAPI) {
        ApiResponse errorResponse = new ApiResponse();
        errorResponse.setDescription("Error");
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new Schema<>().$ref(ERROR));
        content.addMediaType("application/json", mediaType);
        errorResponse.setContent(content);
        Map<String, ApiResponse> apiResponses = openAPI.getComponents().getResponses();
        if (apiResponses == null) {
            apiResponses = new HashMap<>();
        }
        apiResponses.put(ERROR, errorResponse);
        openAPI.getComponents().setResponses(apiResponses);
    }

    public void addErrorSchema() {
        ObjectSchema error = new ObjectSchema();
        error.setRequired(Collections.singletonList(ERROR));
        ObjectSchema objectSchema = new ObjectSchema();
        objectSchema.setRequired(Arrays.asList("code", MESSAGE));
        objectSchema.addProperty("code", new StringSchema());
        objectSchema.addProperty(MESSAGE, new StringSchema());
        objectSchema.addProperty("target", new StringSchema());
        ArraySchema detailsSchema = new ArraySchema();
        ObjectSchema detailsObject = new ObjectSchema();
        detailsObject.setRequired(Arrays.asList("code", MESSAGE));
        detailsSchema.items(detailsObject);
        detailsSchema.addProperty("code", new StringSchema());
        detailsSchema.addProperty(MESSAGE, new StringSchema());
        detailsSchema.addProperty("target", new StringSchema());
        objectSchema.addProperty("details", detailsSchema);
        ObjectSchema innerError = new ObjectSchema();
        innerError.setDescription("The structure of this object is service-specific");
        objectSchema.addProperty("innererror", innerError);
        error.addProperty(ERROR, objectSchema);
        schemas.put(ERROR, error);
    }


    public void createBatchResource(OpenAPI openAPI) {
        PathItem pathItem = new PathItem();
        Operation batchOperation = new Operation();
        List<String> tag = new ArrayList<>();
        tag.add("Batch Requests");
        batchOperation.setTags(tag);
        batchOperation.setSummary("Send a group of requests");
        batchOperation.setDescription("Group multiple requests into a single request payload, see [Batch Requests](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_BatchRequests).\n\n*Please note that \"Try it out\" is not supported for this request.*");
        pathItem.operation(HttpMethod.valueOf("POST"), batchOperation);
        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(true);
        requestBody.setDescription("Batch request");
        StringSchema requestSchema = new StringSchema();
        MediaType requestMediaType = new MediaType();
        requestMediaType.setSchema(requestSchema);
        requestSchema.setExample("--request-separator\nContent-Type: application/http\nContent-Transfer-Encoding: binary\n\nGET People HTTP/1.1\nAccept: application/json\n\n\n--request-separator--");
        Content requestContent = new Content();
        requestContent.addMediaType("multipart/mixed;boundary=request-separator", requestMediaType);
        requestBody.setContent(requestContent);
        batchOperation.setRequestBody(requestBody);
        ApiResponses responses = new ApiResponses();
        ApiResponse response200 = new ApiResponse();
        response200.setDescription("Batch request");
        StringSchema responseSchema = new StringSchema();
        MediaType responseMediaType = new MediaType();
        responseSchema.setExample("--request-separator\nContent-Type: application/http\nContent-Transfer-Encoding: binary\n\nGET People HTTP/1.1\nAccept: application/json\n\n\n--request-separator--");
        Content responseContent = new Content();
        responseContent.addMediaType("multipart/mixed", responseMediaType);
        response200.setContent(responseContent);
        responses.addApiResponse("200", response200);
        ApiResponse response4xx = new ApiResponse();
        response4xx.$ref(ERROR);
        responses.addApiResponse("4XX", response4xx);
        batchOperation.setResponses(responses);
        openAPI.getPaths().addPathItem("/$batch", pathItem);
    }

    public List<Tag> getTags() {
        List<Tag> globalTags = new ArrayList<>();
        for (String tagName : knownEntityTags.keySet()) {
            Tag tag = new Tag();
            tag.setName(tagName);
            globalTags.add(tag);
        }
        return globalTags;
    }

    public List<String> addDescToProperties(List<String> properties) {
        List<String> updatedProperty = new ArrayList<>();
        for (String property : properties) {
            updatedProperty.add(property);
            updatedProperty.add(property + " desc");
        }
        return updatedProperty;
    }

    public ArraySchema addArraySchema(List<String> properties) {
        ArraySchema navArraySchema = new ArraySchema();
        navArraySchema.setUniqueItems(true);
        StringSchema navStringSchema = new StringSchema();
        navStringSchema._enum(properties);
        List<Schema> anyOff = new ArrayList<>();
        anyOff.add(navStringSchema);
        StringSchema stringSchema = new StringSchema();
        anyOff.add(stringSchema);
        navArraySchema.setItems(new Schema<>().anyOf(anyOff));
        return navArraySchema;
    }

    private PathItem getPathItemForEntity(Edm edm, EdmBindingTarget entity, boolean idPath) throws EdmException {
        PathItem pathItem = new PathItem();
        String entityName = entity.getName();
        EdmEntityType entityType = entity.getEntityType();
        logger.debug("entityName: {} Container: {}", entityName, entity.getEntityContainer());
        if (idPath) {
            // All Key-Properties are mapped to a general Id parameter, and we only document it
            StringBuilder paramIdDescription = new StringBuilder("Id supports: ");
            for (EdmKeyPropertyRef key : entityType.getKeyPropertyRefs()) {
                paramIdDescription.append(key.getName()).append(", ");
            }
            paramIdDescription = new StringBuilder(paramIdDescription.substring(0, paramIdDescription.length() - 2));
            Parameter param = new PathParameter();
            param.setName("Id");
            param.setSchema(new StringSchema());
            param.setDescription(paramIdDescription.toString());
            pathItem.addParametersItem(param);
        }
        List<String> tag = new ArrayList<>();
        tag.add(entityName);
        knownEntityTags.put(entityName, entityName);
        Operation operation;
        ApiResponses responses;
        // GET Method
        operation = new Operation();
        operation.setTags(tag);
        if (idPath) {
            operation.setSummary("Get " + entityName + " on Id");
            operation.setOperationId("get" + entityName + "Id");
        } else {
            operation.setSummary("Get " + entityName);
            operation.setOperationId("get" + entityName);
        }

        String operationDescription = "Returns the entity: " + entityName + ". "
            + "For more information on how to access entities visit: <a target=\"_blank\" href=\"https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#sec_AddressingEntities\">Addressing Entities</a>";
        List<String> structProperties = entityType.getPropertyNames();
        if (entityType.getNavigationPropertyNames() != null && !entityType.getNavigationPropertyNames().isEmpty()) {
            List<String> navProperties = new ArrayList<>(entityType.getNavigationPropertyNames());
            operationDescription += "<br /><br />The entity: " + entityName + " supports the following navigational properties: " + navProperties;
            operationDescription += "<br />For example: .../" + entityName + "(Entity-Id)/<b>" + navProperties.get(0) + "</b>/.....";
            ArraySchema navArraySchema = addArraySchema(navProperties);
            operation.addParametersItem(createParameter("$expand", "Expand related entities, see [Expand](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionexpand)", navArraySchema, getExample(navProperties)));

        } else {
            operationDescription += "<br />The entity: " + entityName + " supports <b>no</b> navigational properties.";
        }
        ArraySchema selectArraySchema = addArraySchema(structProperties);
        operation.setDescription(operationDescription);
        operation.addParametersItem(createParameter("$select", "Select properties to be returned, see [Select](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionselect)", selectArraySchema, getExample(structProperties)));
        if (!idPath) { // When requesting with specific ID the following parameters are not required/meaningful
            operation.addParametersItem(createParameter("$filter", "Filter items by property values, see [Filtering](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionfilter)", new StringSchema()));
            List<String> updatedStructProperties = addDescToProperties(structProperties);
            ArraySchema orderArraySchema = addArraySchema(updatedStructProperties);
            operation.addParametersItem(createParameter("$orderby", "Order items by property values, see [Sorting](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionorderby)", orderArraySchema, getExample(structProperties)));
            IntegerSchema topSchema = new IntegerSchema();
            topSchema.setDefault(10);
            operation.addParametersItem(new Parameter().$ref("top"));
            operation.addParametersItem(new Parameter().$ref("skip"));
            operation.addParametersItem(new Parameter().$ref("search"));
            operation.addParametersItem(new Parameter().$ref("count"));
        }
        ApiResponse response4xx = new ApiResponse();
        response4xx.$ref(ERROR);
        responses = new ApiResponses()
            .addApiResponse("200", createResponse(ENTITY_SET + entityName,
                getSchemaForType(edm, entityType, idPath))).addApiResponse("4XX", response4xx);

        operation.setResponses(responses);
        pathItem.operation(HttpMethod.GET, operation);
        operation.setDescription(operationDescription);


        // POST Method
        operation = new Operation();
        operation.setTags(tag);
        if (idPath) {
            operation.setSummary("Create a new entity " + entityName + " on Id");
            operation.setOperationId("create" + entityName + "Id");
        } else {
            operation.setSummary("Create a new entity " + entityName);
            operation.setOperationId("create" + entityName);
        }
        operation.setDescription("Create a new entity in EntitySet: " + entityName);
        operation.setRequestBody(createRequestBody(edm, entityType, "The entity to create", true));
        responses = new ApiResponses()
            .addApiResponse("201", createResponse(ENTITY_SET + entityName))
            .addApiResponse("4XX", response4xx);
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.POST, operation);

        if (!idPath) return pathItem;
        // PATCH Method
        operation = new Operation();
        operation.setTags(tag);
        operation.setSummary("Update entity " + entityName);
        operation.setOperationId("update" + entityName);
        operation.setDescription("Update an existing entity: " + entityName);
        operation.setRequestBody(createRequestBody(edm, entityType, "The entity to update", true));
        responses = new ApiResponses()
            .addApiResponse("200", createResponse(ENTITY_SET + entityName))
            .addApiResponse("4XX", response4xx);
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.PATCH, operation);

        // DELETE Method
        operation = new Operation();
        operation.setTags(tag);
        operation.setSummary("Delete " + entityName);
        operation.setOperationId("delete" + entityName);
        operation.setDescription("Delete an entity " + entityName);
        responses = new ApiResponses()
            .addApiResponse("204", createResponse("Entity " + entityName + " successfully deleted"))
            .addApiResponse("4XX", response4xx);
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.DELETE, operation);
        return pathItem;
    }

    private String getEntityIdPath(EdmEntitySet entityType) throws EdmException {
        return "/" + entityType.getName() + "({Id})*";
    }

    private String getEntityPath(EdmEntitySet entityType) throws EdmException {
        return "/" + entityType.getName() + "*";
    }

    private String getSingletonIdPath(EdmSingleton edmSingleton) throws EdmException {
        return "/" + edmSingleton.getName() + "({Id})*";
    }

    private String getSingletonPath(EdmSingleton edmSingleton) throws EdmException {
        return "/" + edmSingleton.getName() + "*";
    }

    private PathItem getPathItemForFunction(Edm edm, EdmOperation function) throws EdmException {
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        List<String> tag = new ArrayList<>();
        boolean hasReturnType = function.getReturnType() != null;
        // Add functions to the same group as the entity itself that is returned, but only if already presents
        if (hasReturnType && function.getReturnType().getType().getKind() == EdmTypeKind.ENTITY
            && knownEntityTags.containsKey(function.getReturnType().getType().getName())) {
            tag.add(function.getReturnType().getType().getName());
        } else {
            tag.add("Service operations");
        }
        operation.setTags(tag);
        operation.setOperationId(function.getName());
        for (String parameterName : function.getParameterNames()) {
            EdmParameter param = function.getParameter(parameterName);
            operation.addParametersItem(createParameter(edm, param));
        }
        pathItem.operation(HttpMethod.valueOf("GET"), operation);
        try {
            ApiResponses responses = new ApiResponses()
                .addApiResponse("200", createResponse(
                    function.getReturnType().getType().getName(),
                    getSchemaForType(edm, function.getReturnType().getType(), function.getReturnType().isCollection())))
                ._default(createResponse("Unexpected error"));
            operation.setResponses(responses);
        } catch (Exception e) {
            // Happens for instance, when the given returnType cannot be resolved or is null
            if (hasReturnType) {
                logger.error("Error setting response for function: {} Creating standard response.", function.getName(), e);
            }
            ApiResponses responses = new ApiResponses()
                .addApiResponse("200", createResponse(function.getName(), new StringSchema()))
                ._default(createResponse("Unexpected error"));
            operation.setResponses(responses);
        }
        return pathItem;
    }

    private Parameter createParameter(Edm edm, EdmParameter param) throws EdmException {
        Schema<?> schema = getSchemaForType(edm, param.getType(), param.isCollection());
        Parameter parameter = createParameter(param.getName(), getDescription(param), schema);
        parameter.setRequired(!param.isNullable());
        return parameter;
    }

    private Parameter createParameter(String name, String description, Schema<?> schema) {
        return createParameter(name, description, schema, null);
    }

    private Parameter createParameter(String name, String description, Schema<?> schema, String example) {
        Parameter param = new QueryParameter();
        param.setName(name);
        param.setDescription(description);
        param.setSchema(schema);
        if (example != null) param.setExample(example);
        return param;
    }

    private ApiResponse createResponse(String description) throws EdmException {
        return createResponse(description, null);
    }

    private RequestBody createRequestBody(Edm edm, EdmEntityType entityType, String description, boolean required) {
        RequestBody body = new RequestBody();
        body.setDescription(description);
        body.setRequired(required);
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(getSchemaForType(edm, entityType, false));
        content.addMediaType("application/json", mediaType);
        body.setContent(content);
        return body;
    }

    private Schema<?> getSchemaForType(Edm edm, EdmType type, boolean isCollection) {
        try {
            return type.getKind() == EdmTypeKind.PRIMITIVE ? getSchemaForType(edm, type, false, isCollection) :  getSchemaForType(edm, type, true, isCollection);
        } catch (EdmException e) {
            logger.error("Error getting schema for type: {}", type.getName());
            return null;
        }
    }

    private Schema<Object> getSchemaForType(Edm edm, EdmType type, boolean asRef, boolean isCollection) throws EdmException {
        Schema schema = null;
        if (type.getKind() == EdmTypeKind.PRIMITIVE) {
            schema = getSimpleSchema(type.getName());
        } else if (type.getKind() == EdmTypeKind.ENUM) {
            schema = new StringSchema();
            EdmEnumType enumType = (EdmEnumType) type;
            for (String member : enumType.getMemberNames()) {
                schema.addEnumItemObject(member);
            }
        } else if (type.getKind() == EdmTypeKind.ENTITY || type.getKind() == EdmTypeKind.COMPLEX) {
            EdmStructuredType entityType;
            if (type.getKind() == EdmTypeKind.ENTITY) {
                entityType = edm.getEntityType(new FullQualifiedName(type.getNamespace(), type.getName()));
            } else {
                entityType = edm.getComplexType(new FullQualifiedName(type.getNamespace(), type.getName()));
            }
            // Check, if the type has been created already
            if (schemas.containsKey(type.getName())) {
                schema = schemas.get(type.getName());
            } else {
                // Create an ObjectSchema that contains all declared properties
                schema = new ObjectSchema();
                for (String propertyName : entityType.getPropertyNames()) {
                    EdmProperty property = (EdmProperty) entityType.getProperty(propertyName);
                    logger.debug("Property : {}", property);
                    Schema<Object> propSchema = getSchemaForType(edm, property.getType(), true, property.isCollection());
                    logger.debug("propSchema : {}", propSchema);

                    if (propSchema != null) {
                        propSchema.setMaxLength(property.getMaxLength());
                        propSchema.setDefault(property.getDefaultValue());
                        propSchema.setNullable(property.isNullable());
                        schema.addProperty(propertyName, propSchema);
                    }
                }
                EdmStructuredType typeImpl = (EdmStructuredType) type;
                schema.setDescription(getDescription(typeImpl));
            }
            schemas.put(type.getName(), schema);
            if (asRef) {
                Schema schemaRef = new Schema<>().$ref(type.getName());
                List<Schema> allOff = new ArrayList<>();
                allOff.add(schemaRef);
                return new Schema<>().allOf(allOff);
            }
        }
        if (isCollection) {
            Schema collectionSchema = new ArraySchema();
            collectionSchema.setItems(schema);
            return collectionSchema;
        } else {
            return schema;
        }
    }

    private String getDescription(EdmAnnotatable entity) {
        return null;
    }

    private String getExample(List<String> possibleExamples) {
        // Avoid providing an example such ID, id as it is in most cases not the best option for an example
        for (String example : possibleExamples) {
            if (!example.equalsIgnoreCase("id")) return example;
        }
        return null;
    }

    @Override
    public void filterAPISpecification() {
        if (filterConfig == null) return;
        OpenAPI3SpecificationFilter.filter(openAPI, filterConfig);
    }
}
