package com.axway.apim.api.apiSpecification;

import com.axway.apim.lib.errorHandling.AppException;
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
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ODataV4Specification extends ODataSpecification {

    private final Logger logger = LoggerFactory.getLogger(ODataV4Specification.class);

    private final Map<String, Schema> schemas = new HashMap<>();
    private final Map<String, String> knownEntityTags = new HashMap<>();
    private final Map<String, String> namespaceAliasMap = new HashMap<>();
    private final Map<FullQualifiedName, EdmAnnotations> entityAnnotations = new HashMap<>();

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        return APISpecType.ODATA_V4;
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        try {
            super.parse(apiSpecificationContent);
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
            addServer(openAPI);
            List<EdmSchema> edmSchemas = edm.getSchemas();
            for (EdmSchema schema : edmSchemas) {
                info.setTitle(schema.getNamespace() + " OData Service");
                namespaceAliasMap.put(schema.getNamespace(), schema.getAlias());
                for (EdmAnnotations annotationGroup : schema.getAnnotationGroups()) {
                    logger.info("Target path : {}", annotationGroup.getTargetPath());
                    entityAnnotations.put(new FullQualifiedName(annotationGroup.getTargetPath()), annotationGroup);
                }
                for (EdmEntitySet entityType : schema.getEntityContainer().getEntitySets()) {
                    openAPI.path(getEntityPath(entityType), getPathItemForEntity(edm, entityType, false));
                    openAPI.path(getEntityIdPath(entityType), getPathItemForEntity(edm, entityType, true));
                }

                for (EdmSingleton edmSingleton : schema.getEntityContainer().getSingletons()) {
                    openAPI.path(getSingletonPath(edmSingleton), getPathItemForEntity(edm, edmSingleton, false));
                    openAPI.path(getSingletonIdPath(edmSingleton), getPathItemForEntity(edm, edmSingleton, true));
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
            logger.error("Error parsing OData V4 MetaData.", e);
            return false;
        }
    }

    public void addServer(OpenAPI openAPI) {
        String odataUrl = getApiSpecificationFile();
        Server server = new Server();
        if (odataUrl.startsWith("http")) {
            server.setUrl(odataUrl);
        } else {
            server.setUrl("/");
        }
        List<Server> servers = new ArrayList<>();
        servers.add(server);
        openAPI.setServers(servers);
    }

    public void addTopSkipSearchAndCount(OpenAPI openAPI) {
        Parameter topParameter = new Parameter();
        topParameter.setName("$top");
        topParameter.setIn("query");
        IntegerSchema integerSchema = new IntegerSchema();
        integerSchema.setMinimum(BigDecimal.valueOf(0));
        integerSchema.setFormat(null);
        topParameter.setSchema(integerSchema);
        topParameter.setDescription("Show only the first n items, see [Paging - Top](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptiontop)");
        topParameter.setExample(50);
        Map<String, Parameter> parameters = openAPI.getComponents().getParameters();
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put("top", topParameter);

        Parameter skipParameter = new Parameter();
        skipParameter.setName("$skip");
        skipParameter.setIn("query");
        skipParameter.setSchema(integerSchema);
        skipParameter.setDescription("Skip the first n items, see [Paging - Skip](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionskip)");
        parameters.put("skip", skipParameter);

        Parameter countParameter = new Parameter();
        countParameter.setName("$count");
        countParameter.setIn("query");
        BooleanSchema booleanSchema = new BooleanSchema();
        countParameter.setSchema(booleanSchema);
        countParameter.setDescription("Include count of items, see [Count](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptioncount)");
        parameters.put("count", countParameter);

        Parameter searchParameter = new Parameter();
        searchParameter.setName("$search");
        searchParameter.setIn("query");
        StringSchema stringSchema = new StringSchema();
        searchParameter.setSchema(stringSchema);
        searchParameter.setDescription("Search items by search phrases, see [Searching](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionsearch)");
        parameters.put("search", searchParameter);
        openAPI.getComponents().setParameters(parameters);
    }

    public void createBatchResource(OpenAPI openAPI) {

        PathItem pathItem = new PathItem();
        Operation batchOperation = new Operation();
        List<String> tag = new ArrayList<>();
        tag.add("Batch Requests");
        batchOperation.setTags(tag);
        batchOperation.setSummary("Send a group of requests");
        batchOperation.setDescription("Group multiple requests into a single request payload, see [Batch Requests](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_BatchRequests).\n\n*Please note that \"Try it out\" is not supported for this request.*");

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
        //  response200.se
        response200.setDescription("Batch request");
        StringSchema responseSchema = new StringSchema();
        MediaType responseMediaType = new MediaType();
        //  response200.setContent(new Content().addMediaType(MediaType));

        responseSchema.setExample("--request-separator\nContent-Type: application/http\nContent-Transfer-Encoding: binary\n\nGET People HTTP/1.1\nAccept: application/json\n\n\n--request-separator--");
        Content responseContent = new Content();
        responseContent.addMediaType("multipart/mixed", requestMediaType);
        response200.setContent(responseContent);
        responses.addApiResponse("200", response200);

        ApiResponse response4xx = new ApiResponse();
        response4xx.$ref("error");
        responses.addApiResponse("4XX", response4xx);
        batchOperation.setResponses(responses);
        openAPI.getPaths().addPathItem("/$batch",pathItem);

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

    private PathItem getPathItemForEntity(Edm edm, EdmBindingTarget entity, boolean idPath) throws EdmException {
        PathItem pathItem = new PathItem();
        String entityName = entity.getName();
        EdmEntityType entityType = entity.getEntityType();
        logger.info("entityName: {} Container: {}", entityName, entity.getEntityContainer());
        if (idPath) {
            // All Key-Properties are mapped to a general Id parameter and we only document it
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
        if (getTitle(entity) == null) {
            tag.add(entityName);
            knownEntityTags.put(entityName, entityName);
        } else {
            String title = getTitle(entity);
            tag.add(title);
            knownEntityTags.put(title, title);
        }

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
        List<String> navProperties = new ArrayList<>();
        List<String> structProperties = entityType.getPropertyNames();
        if (entityType.getNavigationPropertyNames() != null && entityType.getNavigationPropertyNames().size() > 0) {
            navProperties.addAll(entityType.getNavigationPropertyNames());
            operationDescription += "<br /><br />The entity: " + entityName + " supports the following navigational properties: " + navProperties;
            operationDescription += "<br />For example: .../" + entityName + "(Entity-Id)/<b>" + navProperties.get(0) + "</b>/.....";
            ArraySchema navArraySchema = new ArraySchema();
            navArraySchema.setUniqueItems(true);

            StringSchema navStringSchema = new StringSchema();
            navStringSchema._enum(navProperties);
            navArraySchema.setItems(navStringSchema);
            operation.addParametersItem(createParameter("$expand", "Expand related entities, see [Expand](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionexpand)", navArraySchema, getExample(navProperties)));

        } else {
            operationDescription += "<br />The entity: " + entityName + " supports <b>no</b> navigational properties.";
        }
        ArraySchema selectArraySchema = new ArraySchema();
        selectArraySchema.setUniqueItems(true);
        StringSchema selectStringSchema = new StringSchema();
        selectStringSchema._enum(structProperties);
        selectArraySchema.setItems(selectStringSchema);
        operation.setDescription(operationDescription);
        operation.addParametersItem(createParameter("$select", "Select properties to be returned, see [Select](http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionselect)", selectArraySchema, getExample(structProperties)));
        if (!idPath) { // When requesting with specific ID the following parameters are not required/meaningful
            operation.addParametersItem(createParameter("$filter", "Filter items by property values, see [Filtering](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionfilter)", new StringSchema()));
            ArraySchema orderArraySchema = new ArraySchema();
            orderArraySchema.setUniqueItems(true);
            List<String> updatedProperties = addDescToProperties(structProperties);
            StringSchema stringSchema = new StringSchema();
            stringSchema._enum(updatedProperties);
            orderArraySchema.setItems(stringSchema);
            operation.addParametersItem(createParameter("$orderby", "Order items by property values, see [Sorting](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionorderby)", orderArraySchema, getExample(structProperties)));
            IntegerSchema topSchema = new IntegerSchema();
            topSchema.setDefault(10);
            operation.addParametersItem(new Parameter().$ref("top"));
            operation.addParametersItem(new Parameter().$ref("skip"));
            operation.addParametersItem(new Parameter().$ref("search"));
            operation.addParametersItem(new Parameter().$ref("count"));
        }
        responses = new ApiResponses()
                .addApiResponse("200", createResponse("EntitySet " + entityName,
                        getSchemaForType(edm, entityType, idPath)))
                ._default(createResponse("Unexpected error"));
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.GET, operation);
        operation.setDescription(operationDescription);

        if (!idPath) return pathItem;

        // POST Method
        operation = new Operation();
        operation.setTags(tag);
        operation.setSummary("Create a new entity " + entityName);
        operation.setOperationId("create" + entityName);
        operation.setDescription("Create a new entity in EntitySet: " + entityName);
        operation.setRequestBody(createRequestBody(edm, entityType, "The entity to create", true));

        responses = new ApiResponses()
                .addApiResponse("201", createResponse("EntitySet " + entityName))
                ._default(createResponse("Unexpected error"));
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.POST, operation);

        // PATCH Method
        operation = new Operation();
        operation.setTags(tag);
        operation.setSummary("Update entity " + entityName);
        operation.setOperationId("update" + entityName);
        operation.setDescription("Update an existing entity: " + entityName);
        operation.setRequestBody(createRequestBody(edm, entityType, "The entity to update", true));

        responses = new ApiResponses()
                .addApiResponse("200", createResponse("EntitySet " + entityName))
                ._default(createResponse("Unexpected error"));
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
                ._default(createResponse("Unexpected error"));
        operation.setResponses(responses);
        pathItem.operation(HttpMethod.DELETE, operation);

        return pathItem;
    }


    public StringSchema getSchemaAllowedValues(Enum[] allowedValues) {
        StringSchema schema = new StringSchema();
        for (Enum allowedValue : allowedValues) {
            schema.addEnumItemObject(allowedValue.name());
        }
        return schema;
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
        // setFunctionDocumentation(function, operation);
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
                logger.error("Error setting response for function: " + function.getName() + ". Creating standard response.", e);
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

    private ApiResponse createResponse(String description, Schema<?> schema) throws EdmException {
        ApiResponse response = new ApiResponse();
        response.setDescription(description);
        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(schema);
        content.addMediaType("application/json", mediaType);
        response.setContent(content);
        return response;
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


	/*private Schema<?> getSchemaForType(EdmType type) throws EdmException {
		return getSchemaForType(type, EdmMultiplicity.ONE);
	}*/

    private Schema<?> getSchemaForType(Edm edm, EdmType type, boolean isCollection) {
        try {
            if (type.getKind() == EdmTypeKind.PRIMITIVE) {
                return getSchemaForType(edm, type, false, isCollection);
            } else {
                return getSchemaForType(edm, type, true, isCollection);
            }
        } catch (EdmException e) {
            try {
                logger.error("Error getting schema for type: " + type.getName());
            } catch (EdmException e1) {
            }
            return null;
        }
    }

    private Schema<Object> getSchemaForType(Edm edm, EdmType type, boolean asRef, boolean isCollection) throws EdmException {
        Schema schema = null;
        if (type.getKind() == EdmTypeKind.PRIMITIVE) {
            schema = (Schema<Object>) getSimpleSchema(type.getName());
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
                    Schema<Object> propSchema = getSchemaForType(edm, property.getType(), true, property.isCollection());
                    propSchema.setMaxLength(property.getMaxLength());
                    propSchema.setDefault(property.getDefaultValue());
                    propSchema.setNullable(property.isNullable());
                    //propSchema.setTitle(property. );
                    // propSchema.setDescription(property.);
                    schema.addProperties(propertyName, propSchema);
                }
                EdmStructuredType typeImpl = (EdmStructuredType) type;
                schema.setDescription(getDescription(typeImpl));
                schema.setTitle(getTitle(typeImpl));
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
        try {
            String summary = null;
            String longDescription = null;
            String quickInfo = getQuickInfo(entity);
            if (entity.getAnnotations() == null) return null;
            for (EdmAnnotation annoElem : entity.getAnnotations()) {
				/*if("documentation".equals(annoElem. getName().toLowerCase())) {
					for(EdmAnnotationElement child : annoElem.getChildElements()) {
						if("summary".equals(child.getName().toLowerCase())) {
							summary = child.getText();
						}
						if("longdescription".equals(child.getName().toLowerCase())) {
							longDescription = child.getText();
							continue;
						}
					}
				}*/
            }
            if (summary == null && longDescription == null && quickInfo == null) return null;
            String description = "";
            if (quickInfo != null) description = quickInfo;
            if (!description.equals("") && summary != null) description += "<br />";
            if (summary != null) description += summary;
            if (!description.equals("") && longDescription != null) description += "<br />";
            if (longDescription != null) description += longDescription;
            return description;
        } catch (EdmException e) {
            return null;
        }
    }

    private String getTitle(EdmAnnotatable entity) {
        return getFromAnnotationAttributes(entity, "label");
    }

    private String getQuickInfo(EdmAnnotatable entity) {
        return getFromAnnotationAttributes(entity, "quickinfo");
    }

    private String getFromAnnotationAttributes(EdmAnnotatable entity, String annotationName) {
        entity = entity;
        try {
			/*if(entity.getAnnotations()!=null && entity.getAnnotations().getAnnotationAttributes()!=null) {
				for(EdmAnnotationAttribute attribute : entity.getAnnotations().getAnnotationAttributes()) {
					if(annotationName.equals(attribute.getName().toLowerCase())) {
						return attribute.getText();
					}
				}
			}*/
            return null;
        } catch (EdmException e) {
            return null;
        }
    }

    private String getExample(List<String> possibleExamples) {
        // Avoid providing a example such ID, id as it is in most cases not the best option for an example
        for (String example : possibleExamples) {
            if (!example.equalsIgnoreCase("id")) return example;
        }
        return null;
    }

    private boolean isEntityInsertable(Edm edm, EdmEntitySet entity) {
        EdmAnnotations annotations = getEntityAnnotations(entity);
        EdmTerm term = edm.getTerm(new FullQualifiedName("SAP__self", "SAP__capabilities.SearchRestrictions"));
        //annotations.getAnnotation(, null);
        return annotations == null;

		/*for(EdmAnnotation annotation : annotations.getAnnotationGroup().getAnnotations()) {
			annotation = annotation;
			EdmAnnotationsImp
			//EdmTerm term = edm.getTerm(new FullQualifiedName("SAP__capabilities.SearchRestrictions"));

		}*/
    }

    private EdmAnnotations getEntityAnnotations(EdmEntitySet entity) {
        EdmEntityContainer container = entity.getEntityContainer();
        // Check if an alias exists for the namespace, which might be used for annotation target
        if (this.namespaceAliasMap.get(container.getNamespace()) != null) {
            String namespaceAlias = this.namespaceAliasMap.get(container.getNamespace());
            // Get the annotation using the alias instead of the namespace itself
            return this.entityAnnotations.get(new FullQualifiedName(namespaceAlias, entity.getEntityContainer() + "/" + entity.getName()));
        } else {
            // Otherwise try to get the annotations
            return this.entityAnnotations.get(null);
        }
    }
}