package com.axway.apim.swagger.api.properties.tags;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;

public class TagMap<K, V> extends LinkedHashMap<String, String[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
