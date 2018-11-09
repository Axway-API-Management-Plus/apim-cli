package com.axway.apim.swagger.api;

import com.axway.apim.swagger.api.properties.APIAuthentication;

public class APIBaseDefinition extends AbstractAPIDefinition implements IAPIDefinition {
	
	private String status;

	@Override
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getApiVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public APIAuthentication getAuthentication() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApiName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApiId() {
		// TODO Auto-generated method stub
		return null;
	}
}
