package com.axway.apim.swagger.api;

import com.axway.apim.actions.tasks.props.APIAuthenticationPropertyHandler;
import com.axway.apim.actions.tasks.props.APINamePropertyHandler;
import com.axway.apim.actions.tasks.props.APIPathPropertyHandler;
import com.axway.apim.actions.tasks.props.APISummaryPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.api.properties.APIAuthentication;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAPIDefinition {
	
	protected CommandParameters cmd = CommandParameters.getInstance();
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = APIPathPropertyHandler.class)
	protected String apiPath = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected String status = "NOT_SET";
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = APIAuthenticationPropertyHandler.class)
	protected APIAuthentication authentication = null;
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {})
	protected APISwaggerDefinion swaggerDefinition = null;
	
	@APIPropertyAnnotation(isBreaking = false, writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, propHandler = APINamePropertyHandler.class)
	protected String apiName = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APISummaryPropertyHandler.class)
	protected String apiSummary = null;
	
	protected boolean isValid = false;

	public String getApiPath() {
		throw new RuntimeException("This method must be implemented by concrete class.");
	}

	public boolean isValid() {
		return this.isValid;
	}

	public String getStatus() {
		throw new RuntimeException("This method must be implemented by concrete class.");
	}

	public String getOrgId() {
		throw new RuntimeException("This method must be implemented by concrete class.");
	}
	
	public APISwaggerDefinion getSwaggerDefinition() {
		return this.swaggerDefinition;
	}
}
