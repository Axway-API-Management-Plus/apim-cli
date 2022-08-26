package com.axway.apim.config.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;
@JsonPropertyOrder({"type","name","order","properties"})
public class APISecurity {
	
	private String name;
	private String type;
	private int order = 1;
	
	private Map<String, Object> properties;

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

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}