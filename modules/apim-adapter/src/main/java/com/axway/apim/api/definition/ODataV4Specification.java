package com.axway.apim.api.definition;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmException;

import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class ODataV4Specification extends ODataSpecification {
	
	public ODataV4Specification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.ODATA_V4;
	}
	
	@Override
	public boolean configure() throws AppException {
		try {
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode edm = xmlMapper.readTree(apiSpecificationContent);
			/*SchemaBasedEdmProvider edm = new MetadataParser().buildEdmProvider(new InputStreamReader(new ByteArrayInputStream(apiSpecificationContent)));
			
			this.openAPI = new OpenAPI();
			Info info = new Info();
			info.setTitle("OData Service");
			info.setDescription("The OData Service from " + apiSpecificationFile);
			openAPI.setInfo(info);
			// Iterate over all given Entities
			for(CsdlEntitySet entitySet : edm.getEntityContainer().getEntitySets()) {
				openAPI.path(getPathForEntity(entitySet, edm), getPathItemForEntity(entitySet, edm));
			}
			// Iterate over all Functions
			for(CsdlSchema schema : edm.getSchemas()) {
				for(CsdlFunction function : schema.getFunctions()) {
					if(function.isBound()) continue;
					//openAPI.path(function.getName(), getPathItemForFunction(function, edm));
				}
			}
			List test = edm.getFunctions(new FullQualifiedName(null, null));*/
			return true;
		} catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				LOG.error("Error parsing OData specification.", e);
			}
			return false;
		}
	}
	
	private String getPathForEntity(String entity, String edm) throws EdmException {
		/*String entityName = entity.getName();
		String entityTypeName = entity.getType();
		String singleEntityPath = "/" + entityName + "(";
		CsdlEntityType entityType = edm.getEntityType(new FullQualifiedName(entityTypeName));

		String singleEntityPath = "/" + entityName + "(";
		
		for(CsdlPropertyRef entityKey : entityType.getKey()) {
			singleEntityPath += "{" + entityKey.getName() + "}, ";
		}
		singleEntityPath = singleEntityPath.substring(0, singleEntityPath.length() - 2);
		singleEntityPath += ")*";*/
		return null;
	}
	
	private PathItem getPathItemForFuntion(String function, String edm) throws EdmException {
		/*PathItem pathItem = new PathItem();
		for(CsdlParameter param : function.getParameters()) {
			Parameter parameter = new QueryParameter();
			parameter.setName(param.getName());
			//parameter.setRequired(param.getTypeFQN());
		}*/
		return null;
	}
	
	private PathItem getPathItemForEntity(String entity, String edm) throws EdmException {
		PathItem pathItem = new PathItem();
		/*String entityName = entity.getName();
		String entityTypeName = entity.getType();
		//CsdlEntityType entityType = edm.getEntityType(new FullQualifiedName(entityTypeName));
	
		// Key-Properties become path parameters
		for(CsdlPropertyRef entityKey : entityType.getKey()) {
			Parameter param = new PathParameter();
			param.setName(entityKey.getName());
			pathItem.addParametersItem(param);
		}
		Operation operation;
		ApiResponses responses;
		// GET Method
		operation = new Operation();
		operation.setSummary("Get EntitySet " + entityName);
		operation.setDescription("Returns the EntitySet " + entityName);
		operation.addParametersItem(createParameter("$expand", "Expand navigation property"));
		operation.addParametersItem(createParameter("$select", "Select structural property"));
		operation.addParametersItem(createParameter("$orderby", "Order by some property"));
		operation.addParametersItem(createParameter("$top", "Top elements"));
		operation.addParametersItem(createParameter("$skip", "Skip elements"));
		operation.addParametersItem(createParameter("$inlinecount", "Include count in response"));
		operation.addParametersItem(createParameter("$format", "Response format"));
		operation.addParametersItem(createParameter("$links", "Response format")); // Looks like it must be path parameter
		
		responses = new ApiResponses()
				.addApiResponse("200", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.GET, operation);
		
		// POST Method
		operation = new Operation();
		operation.setSummary("Post a new entity to EntitySet " + entityName);
		operation.setDescription("Post a new entity to EntitySet " + entityName);
		operation.setRequestBody(createRequestBody("The entity to post", true));
		
		responses = new ApiResponses()
				.addApiResponse("201", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.POST, operation);
		
		// PUT Method
		operation = new Operation();
		operation.setSummary("Update entity in EntitySet " + entityName);
		operation.setDescription("Update entity in EntitySet " + entityName);
		operation.setRequestBody(createRequestBody("The entity to post", true));
		
		responses = new ApiResponses()
				.addApiResponse("200", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.PUT, operation);
		
		// PATCH Method
		operation = new Operation();
		operation = new Operation();
		operation.setSummary("Update entity in EntitySet " + entityName);
		operation.setDescription("Update entity in EntitySet " + entityName);
		operation.setRequestBody(createRequestBody("The entity to patch", true));
		
		responses = new ApiResponses()
				.addApiResponse("200", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.PATCH, operation);
		
		// DELETE Method
		operation = new Operation();
		operation = new Operation();
		operation.setSummary("Delete entity in EntitySet " + entityName);
		operation.setDescription("Delete entity in EntitySet " + entityName);
		
		responses = new ApiResponses()
				.addApiResponse("204", createResponse("EntitySet " + entityName))
				._default(createResponse("Unexpected error"));
		operation.setResponses(responses);
		pathItem.operation(HttpMethod.DELETE, operation);
		*/
		return pathItem;
	}
	
	private Parameter createParameter(String name, String description) {
		Parameter param = new QueryParameter();
		param.setName(name);
		param.setDescription(description);
		return param;
	}
	
	private ApiResponse createResponse(String description) {
		ApiResponse response = new ApiResponse();
		response.setDescription(description);
		return response;
	}
	
	private RequestBody createRequestBody(String description, boolean required) {
		RequestBody body = new RequestBody();
		body.setDescription(description);
		body.setRequired(required);
		Content content = new Content();
		MediaType mediaType = new MediaType();
		mediaType.setSchema(new StringSchema());
		content.addMediaType("application/json", mediaType);
		body.setContent(content);
		return body;
	}
}
