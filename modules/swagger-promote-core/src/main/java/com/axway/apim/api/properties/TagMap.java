package com.axway.apim.api.properties;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TagMap<K, V> extends LinkedHashMap<String, String[]> {

	protected ObjectMapper objectMapper = new ObjectMapper();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TagMap() {
		super();
		// TODO Auto-generated constructor stub
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
