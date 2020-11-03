package com.axway.apim.api.model;

import java.util.Map;

public interface CustomPropertiesEntity {
	String getId();
	
	Map<String, String> getCustomProperties();
	
	void setCustomProperties(Map<String, String> customProperties);
}
