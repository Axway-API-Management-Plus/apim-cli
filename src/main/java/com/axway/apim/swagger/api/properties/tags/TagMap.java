package com.axway.apim.swagger.api.properties.tags;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

public class TagMap<K, V> extends LinkedHashMap<String, String[]> {

	protected ObjectMapper objectMapper = new ObjectMapper();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TagMap(JsonNode config) throws AppException {
		super();
		if(config instanceof MissingNode) {
			return;
		}
		try {
			this.putAll((Map<String, String[]>)objectMapper.readValue( config.toString(), new TypeReference<Map<String, String[]>>(){} ));
		} catch (Exception e) {
			throw new AppException("Cant initialize APIImport definition", ErrorCode.UNXPECTED_ERROR, e);
		}
	}



	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		
		if (!(o instanceof TagMap)) return false;
		
		TagMap<String,String[]> otherTagMap = (TagMap<String,String[]>) o;
		
		if (otherTagMap.size() != size()) return false;
		
		Iterator<String> it = this.keySet().iterator();
		
		while(it.hasNext()) {
			String tagName = it.next();
			if(!otherTagMap.containsKey(tagName)) return false;
			String[] myTags = this.get(tagName);
			String[] otherTags = otherTagMap.get(tagName);
			if(!ArrayUtils.isEquals(myTags, otherTags)) return false;
		}
		return true;
	}

}
