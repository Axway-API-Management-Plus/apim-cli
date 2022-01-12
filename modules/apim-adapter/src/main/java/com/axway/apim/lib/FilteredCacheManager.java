package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.config.Builder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter.CacheType;

public class FilteredCacheManager implements CacheManager {
	
	private static Logger LOG = LoggerFactory.getLogger(FilteredCacheManager.class);
	
	private CacheManager cacheManager;
	
	private List<String> enabledCaches;

	public FilteredCacheManager(CacheManager cacheManager) {
		super();
		this.cacheManager = cacheManager;
	}

	public List<String> getEnabledCaches() {
		return enabledCaches;
	}

	public void setEnabledCaches(List<CacheType> enabledCaches) {
		if(this.enabledCaches!=null) return;
		if(enabledCaches==null || cacheManager instanceof DoNothingCacheManager) return;
		this.enabledCaches = new ArrayList<String>();
		for(CacheType cacheType : enabledCaches) {
			this.enabledCaches.add(cacheType.name());
		}
		LOG.info("Enabled caches: " + this.enabledCaches);
	}

	@Override
	public void close() throws StateTransitionException {
		cacheManager.close();
		
	}

	@Override
	public <K, V> Cache<K, V> createCache(String arg0, CacheConfiguration<K, V> arg1) {
		return cacheManager.createCache(arg0, arg1);
	}

	@Override
	public <K, V> Cache<K, V> createCache(String arg0, Builder<? extends CacheConfiguration<K, V>> arg1) {
		return cacheManager.createCache(arg0, arg1);
	}

	@Override
	public <K, V> Cache<K, V> getCache(String alias, Class<K> keyType, Class<V> valueType) {
		if(this.enabledCaches==null) {
			// Caches not specified, return requested cache 
			// however, cacheManager might be a DoNothingCacheManager if ignoreCache is set
			return cacheManager.getCache(alias, keyType, valueType);
		} else {
			if(this.enabledCaches.contains(alias)) {
				LOG.debug("Using cache: " + alias + " as it is enabled.");
				return cacheManager.getCache(alias, keyType, valueType);
			} else {
				Cache<K, V> doNothingCache = new DoNothingCacheManager.DoNothingCache<K, V>();
				return doNothingCache;
			}
		}
	}

	@Override
	public Configuration getRuntimeConfiguration() {
		return cacheManager.getRuntimeConfiguration();
	}

	@Override
	public Status getStatus() {
		return cacheManager.getStatus();
	}

	@Override
	public void init() throws StateTransitionException {
		cacheManager.init();
	}

	@Override
	public void removeCache(String arg0) {
		cacheManager.removeCache(arg0);
	}
	
	
}
