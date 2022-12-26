package com.axway.apim.lib;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.lib.DoNothingCacheManager.DoNothingCache;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.config.Builder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class APIMCLICacheManager implements CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(APIMCLICacheManager.class);

    private final CacheManager cacheManager;

    private List<String> enabledCaches;

    public APIMCLICacheManager(CacheManager cacheManager) {
        super();
        this.cacheManager = cacheManager;
    }

    public void setEnabledCaches(List<CacheType> enabledCaches) {
        if (this.enabledCaches != null) return;
        if (enabledCaches == null || cacheManager instanceof DoNothingCacheManager) return;
        this.enabledCaches = new ArrayList<>();
        for (CacheType cacheType : enabledCaches) {
            if (cacheType.supportsImportActions) {
                this.enabledCaches.add(cacheType.name());
            } else {
                LOG.error("The cache: {} is currently not supported for import actions.", cacheType.name());
            }
        }
        LOG.info("Enabled caches: {}", this.enabledCaches);
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
        String cachePrefix;
        try {
            cachePrefix = CoreParameters.getInstance().getAPIManagerURL().toString();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        if (this.enabledCaches == null) {
            // Caches not specified, return requested cache
            // however, cacheManager might be a DoNothingCacheManager if ignoreCache is set
            Cache<K, V> plainCache = cacheManager.getCache(alias, keyType, valueType);
            return new APIMCLICache<>(plainCache, cachePrefix);
        } else {
            if (this.enabledCaches.contains(alias)) {
                LOG.debug("Using cache: {} as it is enabled.", alias);
                Cache<K, V> plainCache = cacheManager.getCache(alias, keyType, valueType);
                return new APIMCLICache<>(plainCache, cachePrefix);
            } else {
                return new DoNothingCache<>();
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

    /**
     * There are a number of entities which have references to an API (e.g. QuotaRestrictions).
     * These are stored/maintained with their own ID (quotaId) and cached in Ehcache.
     * But, if the API-ID changes, the cached reference points to an API that no longer exists.
     * This method is used to update all entities in the cache when the API ID of an API
     * changes (e.g. with a Replace Action).
     *
     * @param oldApiId the ID currently used by the cached entities
     * @param newApiId the new ID that must be replaced
     * @throws AppException when the cache cannot be updated.
     */
    public void flipApiId(String oldApiId, String newApiId) throws AppException {
        ObjectMapper mapper = new ObjectMapper();
        Cache<String, String> appQuotaCached = getCache(CacheType.APPLICATIONS_QUOTA_CACHE.name(), String.class, String.class);
        if (appQuotaCached instanceof DoNothingCache) return;
        LOG.debug("Updating ApplicationQuotaCache: Flip API-ID: {} --> {}" , oldApiId, newApiId);
        try {
            appQuotaCached.forEach(entry -> {
                try {
                    String cachedValueString = entry.getValue();
                    JsonNode cachedValue = mapper.readTree(cachedValueString);
                    // As System- and App-Default-Quotas are not cached, they can be ignored
                    if (APIManagerAdapter.APPLICATION_DEFAULT_QUOTA.equals(cachedValue.get("id").asText()) ||
                            APIManagerAdapter.SYSTEM_API_QUOTA.equals(cachedValue.get("id").asText())) {
                        // Do nothing
                    } else {
                        ArrayNode restrictions = cachedValue.withArray("restrictions");
                        for (JsonNode restriction : restrictions) {
                            if (oldApiId.equals(restriction.get("api").asText())) {
                                ((ObjectNode) restriction).replace("api", new TextNode(newApiId));
                                appQuotaCached.replace(entry.getKey(), cachedValue.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("There was an error updating the cache.", e);
                }
            });
        } catch (Exception e) {
            appQuotaCached.clear();
            throw new AppException("Error updating the cache. Cache has been cleared.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
