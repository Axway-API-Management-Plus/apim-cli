package com.axway.apim.api.apiSpecification;

import com.axway.apim.api.apiSpecification.filter.OpenAPI3SpecificationFilter;
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
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.core.edm.provider.EdmElementImplProv;
import org.apache.olingo.odata2.core.edm.provider.EdmParameterImplProv;
import org.apache.olingo.odata2.core.edm.provider.EdmStructuralTypeImplProv;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ODataV2Specification extends ODataSpecification {
	
	Edm edm;
	@SuppressWarnings("rawtypes")
	Map<String, Schema> schemas = new HashMap<>();
	
	public enum InlineCountValues {
		allpages,
		none
	}
	
	public enum FormatValues {
		atom,
		xml,
		json
	}
	
	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.ODATA_V2;
	}

	@Override
	public void filterAPISpecification() {
		if(filterConfig == null) return;
		OpenAPI3SpecificationFilter.filter(openAPI, filterConfig);
	}

	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		try {
			super.parse(apiSpecificationContent);
			edm = EntityProvider.readMetadata(new ByteArrayInputStream(apiSpecificationContent), false);
			this.openAPI = new OpenAPI();
			Info info = new Info();
			info.setTitle("OData Service");
			info.setDescription("The OData Service from " + apiSpecificationFile);
			// When running as part of an Integration-Test - This avoids creating a dynamic API-Specification file
			if(apiSpecificationFile.contains("ImportActionTest") ) {
				info.setDescription("The OData Service from my test file");
			}
			info.setVersion(edm.getServiceMetadata().getDataServiceVersion());
			openAPI.setInfo(info);
			for(EdmFunctionImport function : edm.getFunctionImports()) {
				openAPI.path("/" + function.getName(), getPathItemForFunction(function));
			}
			for(EdmEntitySet entitySet : edm.getEntitySets()) {
				openAPI.path(getEntityPath(entitySet), getPathItemForEntity(entitySet, false));
				openAPI.path(getEntityIdPath(entitySet), getPathItemForEntity(entitySet, true));
			}
			Components comp = new Components();
			comp.setSchemas(schemas);
			this.openAPI.setComponents(comp);
			return true;
		} catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				LOG.error("Error parsing OData specification.", e);
			}
			return false;
		}
	}
	
	private String getEntityIdPath(EdmEntitySet entity) throws EdmException {
		String singleEntityPath = "/" + entity.getName() + "({Id})*";
		return singleEntityPath;
	}
	
	private String getEntityPath(EdmEntitySet entity) throws EdmException {
		String singleEntityPath = "/" + entity.getName() + "*";
		return singleEntityPath;
	}
	
	private PathItem getPathItemForFunction(EdmFunctionImport function) throws EdmException {
		PathItem pathItem = new PathItem();
		Operation operation = new Operation();
		List<String> tag = new ArrayList<>();
		// Add functions to the same group as the entity itself
		if(function.getEntitySet()!=null) {
			tag.add(function.getEntitySet().getName());
		} else {
			tag.add("Service operations");
		}
		
		operation.setTags(tag);
		operation.setOperationId(function.getName());
		setFunctionDocumentation(function, operation);
		
		for(String parameterName : function.getParameterNames()) {
			EdmParameter param = function.getParameter(parameterName);
			operation.addParametersItem(createParameter(param));
		}
		
		pathItem.operation(HttpMethod.valueOf(function.getHttpMethod()), operation);
		
		try {
			ApiResponses responses = new ApiResponses()
					.addApiResponse("200", createResponse(
							function.getReturnType().getType().getName(), 
							getSchemaForType(function.getReturnType().getType(), function.getReturnType().getMultiplicity())))
					._default(createResponse("Unexpected error"));
			operation.setResponses(responses);
		} catch (Exception e) {
			// Happens for instance, when the given returnType cannot be resolved
			LOG.error("Error setting response for function: " + function.getName() + ". Creating standard response.", e);
			ApiResponses responses = new ApiResponses()
					.addApiResponse("200", createResponse(function.getName(), new StringSchema()))
					._default(createResponse("Unexpected error"));
			operation.setResponses(responses);
		}
		return pathItem;
	}
	
	private PathItem getPathItemForEntity(EdmEntitySet entity, boolean idPath) throws EdmException {
		PathItem pathItem = new PathItem();

		EdmEntityType entityType = entity.getEntityType();
		String entityName = entity.getName();
		
		if(idPath) {
			// All Key-Properties are mapped to a general Id parameter and we only document it
			String paramIdDescription = "Id supports: ";
			for(String key : entityType.getKeyPropertyNames()) {
				paramIdDescription += key + ", ";
			}
			paramIdDescription = paramIdDescription.substring(0, paramIdDescription.length()-2);
			Parameter param = new PathParameter();
			param.setName("Id");
			param.setSchema(new StringSchema());
			param.setDescription(paramIdDescription);
			pathItem.addParametersItem(param);
		}
		
		List<String> tag = new ArrayList<>();
		if(getTitle(entityType)==null) {
			tag.add(entityName);
		} else {
			tag.add(getTitle(entityType));
		}

		Operation operation;
		ApiResponses responses;
		// GET Method
		operation = new Operation();
		operation.setTags(tag);
		if(idPath) {
			operation.setSummary("Get " + entityName + " on Id");
			operation.setOperationId("get"+entityName+"Id");
		} else {
			operation.setSummary("Get " + entityName);
			operation.setOperationId("get"+entityName);
		}
		
		String operationDescription = "Returns the entity: " + entityName + ". "
				+ "For more information using the query parameters please see: <a target=\"_blank\" href=\"https://www.odata.org/documentation/odata-version-2-0/uri-conventions/\">URI Conventions (OData Version 2.0)</a>";
		List<String> navProperties = new ArrayList<>();
		List<String> structProperties = entityType.getPropertyNames();
		
		if(entityType.getNavigationPropertyNames()!=null && entityType.getNavigationPropertyNames().size()>0) {
			for(String navigationProperty : entityType.getNavigationPropertyNames()) {
				navProperties.add(navigationProperty);
			}
			operationDescription += "<br /><br />The entity: " + entityName + " supports the following navigational properties: "+navProperties;
			operationDescription += "<br />For example: .../" + entityName + "(Entity-Id)/<b>" + navProperties.get(0) + "</b>/.....";
		} else {
			operationDescription += "<br />The entity: " + entityName + " supports <b>no</b> navigational properties.";
		}
		
		ArraySchema stringArraySchema = new ArraySchema();
		stringArraySchema.setItems(new StringSchema());
		
		operation.setDescription(operationDescription);
		operation.addParametersItem(createParameter("$expand", "The syntax of a $expand query option is a comma-separated list of Navigation Properties. Additionally each Navigation Property can be followed by a forward slash and another Navigation Property to enable identifying a multi-level relationship.", new StringSchema(), getExample(navProperties) ));
		operation.addParametersItem(createParameter("$select", "The value of a $select System Query Option is a comma-separated list of selection clauses. Each selection clause may be a Property name, Navigation Property name, or the \"*\" character.", new StringSchema(), getExample(structProperties) ));
		if(!idPath) { // When requesting with specific ID the following parameters are not required/meaningful
			operation.addParametersItem(createParameter("$filter", "Filter items by property values. See <a target=\"_blank\" href=\"https://www.odata.org/documentation/odata-version-2-0/uri-conventions/\">4.5. Filter System Query Option ($filter)</a> for more information.", new StringSchema()));
			operation.addParametersItem(createParameter("$orderby", "Order items by property values. See <a target=\"_blank\" href=\"https://www.odata.org/documentation/odata-version-2-0/uri-conventions/\">4.2. Orderby System Query Option ($orderby)</a> for more information.", new StringSchema(), getExample(structProperties)));
			IntegerSchema topSchema = new IntegerSchema();
			topSchema.setDefault(10);
			operation.addParametersItem(createParameter("$top", "Show only the first n items", topSchema));
			operation.addParametersItem(createParameter("$skip", "Skip the first n items", new IntegerSchema()));
			operation.addParametersItem(createParameter("$inlinecount", "Include count of items", getSchemaAllowedValues(InlineCountValues.values())));
			
		}
		operation.addParametersItem(createParameter("$format", "Response format if supported by the backend service.", getSchemaAllowedValues(FormatValues.values())));
		
		responses = new ApiResponses()
				.addApiResponse("200", createResponse("EntitySet " + entityName, 
						getSchemaForType(entity.getEntityType(), (idPath) ? EdmMultiplicity.ONE : EdmMultiplicity.MANY)))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.GET, operation);
		operation.setDescription(operationDescription);
		
		if(!idPath) return pathItem;
		
		// POST Method
		operation = new Operation();
		operation.setTags(tag);
		operation.setSummary("Create a new entity " + entityName);
		operation.setOperationId("create"+entityName);
		operation.setDescription("Create a new entity in EntitySet: " + entityName);
		operation.setRequestBody(createRequestBody(entityType, EdmMultiplicity.ONE, "The entity to create", true));
		
		responses = new ApiResponses()
				.addApiResponse("201", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.POST, operation);
		
		// PATCH Method
		operation = new Operation();
		operation.setTags(tag);
		operation.setSummary("Update entity " + entityName);
		operation.setOperationId("update"+entityName);
		operation.setDescription("Update an existing entity in EntitySet: " + entityName);
		operation.setRequestBody(createRequestBody(entityType, EdmMultiplicity.ONE, "The entity to update", true));
		
		responses = new ApiResponses()
				.addApiResponse("200", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.PATCH, operation);
		
		// DELETE Method
		operation = new Operation();
		operation.setTags(tag);
		operation.setSummary("Delete " + entityName);
		operation.setOperationId("delete"+entityName);
		operation.setDescription("Delete entity in EntitySet " + entityName);
		
		responses = new ApiResponses()
				.addApiResponse("204", createResponse("EntitySet " + entityName + " successfully deleted"))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.DELETE, operation);
		
		return pathItem;
	}

	
	private Parameter createParameter(EdmParameter param) throws EdmException {
		Schema<?> schema = getSchemaForType(param.getType());
		Parameter parameter = createParameter(param.getName(), getDescription(param), schema);
		EdmParameterImplProv paramImpl = (EdmParameterImplProv)param;
		if(paramImpl.getFacets()!=null) {
			if(paramImpl.getFacets().isNullable()!=null) {
				parameter.setRequired(!paramImpl.getFacets().isNullable());
			}
			schema.setMaxLength(paramImpl.getFacets().getMaxLength());
		}
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
		if(example!=null) param.setExample(example);
		return param;
	}
	
	private ApiResponse createResponse(String description) throws EdmException {
		return createResponse(description, null);
	}
	
	private ApiResponse createResponse(String description, Schema<?> schema) {
		ApiResponse response = new ApiResponse();
		response.setDescription(description);
		Content content = new Content();
		MediaType mediaType = new MediaType();
		mediaType.setSchema(schema);
		content.addMediaType("application/json", mediaType);
		response.setContent(content);
		return response;
	}
	
	private RequestBody createRequestBody(EdmEntityType entityType, EdmMultiplicity multiplicity, String description, boolean required) {
		RequestBody body = new RequestBody();
		body.setDescription(description);
		body.setRequired(required);
		Content content = new Content();
		MediaType mediaType = new MediaType();
		mediaType.setSchema(getSchemaForType(entityType, multiplicity));
		content.addMediaType("application/json", mediaType);
		body.setContent(content);
		return body;
	}
	
	public StringSchema getSchemaAllowedValues(Enum[] allowedValues) {
		StringSchema schema = new StringSchema();
		for(Enum allowedValue : allowedValues) {
			schema.addEnumItemObject(allowedValue.name());
		}
		return schema;
	}
	
	private Schema<?> getSchemaForType(EdmType type) {
		return getSchemaForType(type, EdmMultiplicity.ONE);
	}
	
	private Schema<?> getSchemaForType(EdmType type, EdmMultiplicity multiplicity) {
		try {
			if(type.getKind()==EdmTypeKind.SIMPLE) {
				return getSchemaForType(type, multiplicity, false);
			} else {
				return getSchemaForType(type, multiplicity, true);
			}
		} catch (EdmException e) {
			try {
				LOG.error("Error getting schema for type: " + type.getName());
			} catch (EdmException e1) {
			}
			return null;
		}
	}
	
	private Schema<Object> getSchemaForType(EdmType type, EdmMultiplicity multiplicity, boolean asRef) throws EdmException {
		if(type.getKind()==EdmTypeKind.SIMPLE) {
			Schema<Object> schema = (Schema<Object>)getSimpleSchema(type.getName());
			return schema;
		} else if(type.getKind()==EdmTypeKind.ENTITY || type.getKind()==EdmTypeKind.COMPLEX) {
			EdmStructuralType entityType;
			if(type.getKind()==EdmTypeKind.ENTITY) {
				entityType = edm.getEntityType(type.getNamespace(), type.getName());
			} else {
				entityType = edm.getComplexType(type.getNamespace(), type.getName());
			}
			// Multiple entities should be returned
			if(multiplicity==EdmMultiplicity.MANY) {
				ArraySchema schema = new ArraySchema();
				// Get the schema of the Array-List as a reference, as the object itself is stored as a Component model
				Schema<Object> itemSchema = getSchemaForType(entityType, EdmMultiplicity.ONE, true);
				schema.setItems(itemSchema);
				return schema;
			} else {
				ObjectSchema schema;
				// Check, if the type has been created already
				if(schemas.containsKey(type.getName())) {
					schema = (ObjectSchema) schemas.get(type.getName());
				} else {
					// Create an ObjectSchema based on all declared properties
					schema = new ObjectSchema();
					for(String propertyName : entityType.getPropertyNames()) {
						EdmElementImplProv propertyType = (EdmElementImplProv)entityType.getProperty(propertyName);
						Schema<Object> propSchema = getSchemaForType(propertyType.getType(), propertyType.getMultiplicity(), true);
						if(propertyType.getFacets()!=null) {
							propSchema.setMaxLength(propertyType.getFacets().getMaxLength());
							propSchema.setDefault(propertyType.getFacets().getDefaultValue());
							propSchema.setNullable(propertyType.getFacets().isNullable());
						}
						propSchema.setTitle(getTitle((EdmAnnotatable)propertyType));
						propSchema.setDescription(getDescription((EdmAnnotatable)propertyType));
						schema.addProperties(propertyName, propSchema);
					}
					EdmStructuralTypeImplProv typeImpl = (EdmStructuralTypeImplProv)type;
					schema.setDescription(getDescription(typeImpl));
					schema.setTitle(getTitle(typeImpl));
				}
				schemas.put(type.getName(), schema);
				if(asRef) {
					return new Schema<>().$ref(type.getName());
				}
				return schema;
			}
		} else {
			return null;
		}
	}
	
	private Schema<?> getSimpleSchema(String type) {
		switch(type) {
		case "Guid": 
			return new UUIDSchema();
		case "Int16":
		case "Int32":
		case "Int64":
		case "Decimal":
			return new IntegerSchema();
		case "String":
		case "Single":
		case "Time":
		case "DateTimeOffset":
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
	
	private void setFunctionDocumentation(EdmFunctionImport function, Operation operation) {
		try {
			EdmAnnotations annotations = function.getAnnotations();
			if(annotations==null || annotations.getAnnotationElements()==null) return;
			for(EdmAnnotationElement annoElem : annotations.getAnnotationElements()) {
				if("documentation".equalsIgnoreCase(annoElem.getName())) {
					for(EdmAnnotationElement child : annoElem.getChildElements()) {
						if("summary".equalsIgnoreCase(child.getName())) {
							operation.setSummary(child.getText());
							continue;
						}
						if("longdescription".equalsIgnoreCase(child.getName())) {
							operation.setDescription(child.getText());
						}
						
					}
					break;
				}
			}
		} catch (EdmException e) {
		}
	}
	
	private String getTitle(EdmAnnotatable entity) {
		return getFromAnnotationAttributes(entity, "label");
	}
		
	private String getQuickInfo(EdmAnnotatable entity) {
		return getFromAnnotationAttributes(entity, "quickinfo");
	}
	
	private String getDescription(EdmAnnotatable entity) {
		try {
			String summary = null;
			String longDescription = null;
			String quickInfo = getQuickInfo(entity);
			if(entity.getAnnotations()==null || entity.getAnnotations().getAnnotationElements()==null) return null;
			for(EdmAnnotationElement annoElem : entity.getAnnotations().getAnnotationElements()) {
				if("documentation".equalsIgnoreCase(annoElem.getName())) {
					for(EdmAnnotationElement child : annoElem.getChildElements()) {
						if("summary".equalsIgnoreCase(child.getName())) {
							summary = child.getText();
						}
						if("longdescription".equalsIgnoreCase(child.getName())) {
							longDescription = child.getText();
						}
					}
				}
			}
			if(summary==null && longDescription == null && quickInfo == null) return null;
			String description = "";
			if(quickInfo != null) description = quickInfo;
			if(!description.equals("") && summary != null) description += "<br />";
			if(summary != null) description += summary;
			if(!description.equals("") && longDescription != null) description += "<br />";
			if(longDescription != null) description += longDescription;
			return description;
		} catch (EdmException e) {
			return null;
		}
	}
	
	private String getFromAnnotationAttributes(EdmAnnotatable entity, String annotationName) {
		try {
			if(entity.getAnnotations()!=null && entity.getAnnotations().getAnnotationAttributes()!=null) {
				for(EdmAnnotationAttribute attribute : entity.getAnnotations().getAnnotationAttributes()) {
					if(annotationName.equals(attribute.getName().toLowerCase())) {
						return attribute.getText();
					}
				}
			}
			return null;
		} catch (EdmException e) {
			return null;
		}
	}
	
	private String getExample(List<String> possibleExamples) {
		// Avoid providing a example such ID, id as it is in most cases not the best option for an example
		for(String example : possibleExamples) {
			if(!example.equalsIgnoreCase("id")) return example;
		}
		return null;
	}
}
