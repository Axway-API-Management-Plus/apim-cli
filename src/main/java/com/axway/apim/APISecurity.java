package com.axway.apim;

import java.util.Properties;

public class APISecurity {
	
	private String name = "Pass Through";
	private String type = "passThrough";
	private int order = 1;
	
	
	private Properties properties;
	
	public APISecurity(){
		properties = new Properties();
		properties.setProperty("subjectIdFieldName", "Pass Through");
		properties.setProperty("removeCredentialsOnSuccess", "true");
		
	}
	
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	

}
