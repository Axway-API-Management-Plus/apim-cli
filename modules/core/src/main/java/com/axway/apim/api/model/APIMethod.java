package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIMethod {
	/**
	 * The ID of the FE-API operation
	 */
	private String id;
	
	private String virtualizedApiId;
	
	private String name;
	
	private String apiId;
	
	/**
	 * The ID of the Backend-API method
	 */
	private String apiMethodId;

	private String summary;

	private String original;
	
	private String descriptionManual;
	
	private String descriptionMarkdown;
	
	private String descriptionUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVirtualizedApiId() {
		return virtualizedApiId;
	}

	public void setVirtualizedApiId(String virtualizedApiId) {
		this.virtualizedApiId = virtualizedApiId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getApiMethodId() {
		return apiMethodId;
	}

	public void setApiMethodId(String apiMethodId) {
		this.apiMethodId = apiMethodId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getDescriptionManual() {
		return descriptionManual;
	}

	public void setDescriptionManual(String descriptionManual) {
		this.descriptionManual = descriptionManual;
	}

	public String getDescriptionMarkdown() {
		return descriptionMarkdown;
	}

	public void setDescriptionMarkdown(String descriptionMarkdown) {
		this.descriptionMarkdown = descriptionMarkdown;
	}

	public String getDescriptionUrl() {
		return descriptionUrl;
	}

	public void setDescriptionUrl(String descriptionUrl) {
		this.descriptionUrl = descriptionUrl;
	}
	
	
}
