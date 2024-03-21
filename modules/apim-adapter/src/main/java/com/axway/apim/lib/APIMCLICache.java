package com.axway.apim.lib;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ehcache.Cache;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.spi.loaderwriter.BulkCacheLoadingException;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.ehcache.spi.loaderwriter.CacheWritingException;

public class APIMCLICache<K, V> implements Cache<K, V> {

	private final Cache<K, V> cache;
	private final String prefix;

	public APIMCLICache(Cache<K, V> cache, String prefix) {
		super();
		this.cache = cache;
		this.prefix = prefix;
	}

	@Override
	public boolean containsKey(K key) {
		return this.cache.containsKey(getPrefixedKey(key));
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public V get(K key) throws CacheLoadingException {
		return this.cache.get(getPrefixedKey(key));
	}

	@Override
	public Map<K, V> getAll(Set<? extends K> arg0) throws BulkCacheLoadingException {
		throw new UnsupportedOperationException("Method getAll is not implemented for the APIMCLICache");
	}

	@Override
	public CacheRuntimeConfiguration<K, V> getRuntimeConfiguration() {
		return this.cache.getRuntimeConfiguration();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return this.cache.iterator();
	}

	@Override
	public void put(K key, V value) throws CacheWritingException {
		this.cache.put(getPrefixedKey(key), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) throws BulkCacheWritingException {
		throw new UnsupportedOperationException("Method putAll is not implemented for the APIMCLICache");
	}

	@Override
	public V putIfAbsent(K key, V value) throws CacheLoadingException, CacheWritingException {
		return this.cache.putIfAbsent(getPrefixedKey(key), value);
	}

	@Override
	public void remove(K key) throws CacheWritingException {
		this.cache.remove(getPrefixedKey(key));
	}

	@Override
	public boolean remove(K key, V value) throws CacheWritingException {
		return this.cache.remove(getPrefixedKey(key), value);
	}

	@Override
	public void removeAll(Set<? extends K> arg0) throws BulkCacheWritingException {
		throw new UnsupportedOperationException("Method removeAll is not implemented for the APIMCLICache");
	}

	@Override
	public V replace(K key, V value) throws CacheLoadingException, CacheWritingException {
		return this.cache.replace(getPrefixedKey(key), value);
	}

	@Override
	public boolean replace(K key, V value1, V value2) throws CacheLoadingException, CacheWritingException {
		return this.cache.replace(getPrefixedKey(key), value1, value2);
	}

	@SuppressWarnings("unchecked")
	private K getPrefixedKey(K key) {
		return (K) (prefix+key);
	}
}
