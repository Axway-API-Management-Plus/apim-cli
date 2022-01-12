package com.axway.apim.lib;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter.CacheType;

public class StandardImportParams extends CoreParameters {
	
	private String config;
	
	private String stageConfig;
	
	private String enabledCaches;
	
	private List<CacheType> enabledCacheTypes = null;

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}
	
	public String getStageConfig() {
		return stageConfig;
	}

	public void setStageConfig(String stageConfig) {
		this.stageConfig = stageConfig;
	}

	public static synchronized StandardImportParams getInstance() {
		return (StandardImportParams)CoreParameters.getInstance();
	}
	
	@Override
	public boolean isIgnoreCache() {
		// Caches are disabled for import actions if not explicitly enabled
		if(getEnabledCaches()==null || super.isIgnoreCache()) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getEnabledCaches() {
		return enabledCaches;
	}

	@Override
	public List<CacheType> getEnabledCacheTypes() {
		if(isIgnoreCache()) return null;
		if(getEnabledCaches()==null) return null;
		if(enabledCacheTypes!=null) return enabledCacheTypes;
		enabledCacheTypes = createCacheList(getEnabledCaches());
		return enabledCacheTypes;
	}

	public void setEnabledCaches(String enabledCaches) {
		this.enabledCaches = enabledCaches;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + ", config=" + config + "]";
	}
}