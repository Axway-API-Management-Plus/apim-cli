package com.axway.apim.lib;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.config.Builder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.config.Configuration;
import org.ehcache.spi.loaderwriter.BulkCacheLoadingException;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.ehcache.spi.loaderwriter.CacheWritingException;

public class DoNothingCacheManager implements CacheManager {
	
	public static class DoNothingCache<K, V> implements Cache<K, V> {

		@Override
		public void clear() {
		}
		@Override
		public boolean containsKey(Object arg0) {
			return false;
		}
		@Override
		public Object get(Object arg0) throws CacheLoadingException {
			return null;
		}
		@Override
		public Map getAll(Set arg0) throws BulkCacheLoadingException {
			return null;
		}
		@Override
		public CacheRuntimeConfiguration getRuntimeConfiguration() {
			return null;
		}
		@Override
		public Iterator iterator() {
			return Collections.emptyIterator();
		}
		@Override
		public void put(Object arg0, Object arg1) throws CacheWritingException {
			
		}
		@Override
		public void putAll(Map arg0) throws BulkCacheWritingException {
			
		}
		@Override
		public Object putIfAbsent(Object arg0, Object arg1) throws CacheLoadingException, CacheWritingException {
			return null;
		}
		@Override
		public void remove(Object arg0) throws CacheWritingException {
			
		}
		@Override
		public boolean remove(Object arg0, Object arg1) throws CacheWritingException {
			return false;
		}
		@Override
		public void removeAll(Set arg0) throws BulkCacheWritingException {
		}
		@Override
		public Object replace(Object arg0, Object arg1) throws CacheLoadingException, CacheWritingException {
			return null;
		}
		@Override
		public boolean replace(Object arg0, Object arg1, Object arg2)
				throws CacheLoadingException, CacheWritingException {
			return false;
		}
	}

	@Override
	public void close() throws StateTransitionException {
	}

	@Override
	public <K, V> Cache<K, V> createCache(String arg0, CacheConfiguration<K, V> arg1) {
		return null;
	}

	@Override
	public <K, V> Cache<K, V> createCache(String arg0, Builder<? extends CacheConfiguration<K, V>> arg1) {
		return null;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String arg0, Class<K> arg1, Class<V> arg2) {
		return new DoNothingCache<>();
	}

	@Override
	public Configuration getRuntimeConfiguration() {
		return null;
	}

	@Override
	public Status getStatus() {
		return Status.AVAILABLE;
	}

	@Override
	public void init() throws StateTransitionException {
		
	}

	@Override
	public void removeCache(String arg0) {
	}
}
