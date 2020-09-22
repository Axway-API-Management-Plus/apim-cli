package com.axway.apim.apiimport.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardImportParams;

public class APIImportParams extends StandardImportParams {
	
	private boolean forceUpdate;
	private boolean useFEAPIDefinition;
	private boolean replaceHostInSwagger;
	
	private boolean changeOrganization = false; 
	
	private String apiDefintion;
	
	public static synchronized APIImportParams getInstance() {
		return (APIImportParams)CoreParameters.getInstance();
	}
	
	@Override
	public boolean isIgnoreCache() {
		// For import action we ignore the cache in all cases!
		return true;
	}

	public boolean isUseFEAPIDefinition() {
		return useFEAPIDefinition;
	}

	public void setUseFEAPIDefinition(boolean useFEAPIDefinition) {
		this.useFEAPIDefinition = useFEAPIDefinition;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public String getApiDefintion() {
		return apiDefintion;
	}

	public void setApiDefintion(String apiDefintion) {
		this.apiDefintion = apiDefintion;
	}

	public boolean isReplaceHostInSwagger() {
		return replaceHostInSwagger;
	}

	public void setReplaceHostInSwagger(boolean replaceHostInSwagger) {
		this.replaceHostInSwagger = replaceHostInSwagger;
	}

	public boolean isChangeOrganization() {
		return changeOrganization;
	}

	public void setChangeOrganization(boolean changeOrganization) {
		this.changeOrganization = changeOrganization;
	}
}
