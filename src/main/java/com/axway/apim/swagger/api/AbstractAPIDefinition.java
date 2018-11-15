package com.axway.apim.swagger.api;

import com.axway.apim.actions.tasks.props.APIAuthenticationPropertyHandler;
import com.axway.apim.actions.tasks.props.APINamePropertyHandler;
import com.axway.apim.actions.tasks.props.APIPathPropertyHandler;
import com.axway.apim.actions.tasks.props.APISummaryPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIAuthentication;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.OutboundProfile;
import com.axway.apim.swagger.api.properties.OutboundProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAPIDefinition {
	
	protected CommandParameters cmd = CommandParameters.getInstance();
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED})
	protected OutboundProfiles outboundProfiles = null;
	
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

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected APIImage apiImage = null;
	
	protected boolean isValid = false;

	public String getApiPath() throws AppException {
		throw new AppException("This method must be implemented by concrete class.", ErrorCode.UNSUPPORTED_FEATURE);
	}

	public boolean isValid() {
		return this.isValid;
	}

	public String getStatus() throws AppException {
		throw new AppException("This method must be implemented by concrete class.", ErrorCode.UNSUPPORTED_FEATURE);
	}

	public String getOrgId() throws AppException {
		throw new AppException("This method must be implemented by concrete class.", ErrorCode.UNSUPPORTED_FEATURE);
	}
	
	public APISwaggerDefinion getSwaggerDefinition() {
		return this.swaggerDefinition;
	}

	public OutboundProfiles getOutboundProfiles() {
		return this.outboundProfiles;
	}
	
}
