package com.axway.apim.lib;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Map.Entry)) return false;
		Map.Entry e = (Map.Entry) o;
		Object key = e.getKey();
		return super.equals(o);
	}
}
