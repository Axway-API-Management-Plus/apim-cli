package com.axway.apim.swagger.api;

import com.axway.apim.actions.tasks.props.APIAuthenticationPropertyHandler;
import com.axway.apim.actions.tasks.props.APINamePropertyHandler;
import com.axway.apim.actions.tasks.props.APIPathPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;

public abstract class AbstractAPIDefinition {
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {}, propHandler = APIPathPropertyHandler.class)
	protected String apiPath = null;

	@APIPropertyAnnotation(isBreaking = true, writableStates = {})
	protected String status = "NOT_SET";
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {}, propHandler = APIAuthenticationPropertyHandler.class)
	protected APIAuthentication authentication = null;
	
	@APIPropertyAnnotation(isBreaking = false, writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, propHandler = APINamePropertyHandler.class)
	protected String apiName = null;
	
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
}
