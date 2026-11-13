package com.axway.apim.adapter;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Makes sure the bundled cacheConfig.xml can be read with the JAXB runtime on the classpath and that
 * the disk persistent caches can be started. Parsing the XML fails at runtime, not at compile time,
 * if Ehcache and JAXB (javax/jakarta) don't match.
 */
public class CacheConfigurationTest {

    @Test
    public void bundledCacheConfigurationCanBeLoadedAndStarted() throws Exception {
        URL cacheConfig = CacheConfigurationTest.class.getResource("/cacheConfig.xml");
        Assert.assertNotNull(cacheConfig, "cacheConfig.xml not found");
        Path tmpDir = Files.createTempDirectory("apim-cli-cache-test");
        String originalTmpDir = System.getProperty("java.io.tmpdir");
        // The configuration persists to ${java.io.tmpdir}/apim-cli-cache. Don't touch the cache of a real CLI.
        System.setProperty("java.io.tmpdir", tmpDir.toString());
        try {
            XmlConfiguration xmlConfig = new XmlConfiguration(cacheConfig);
            Set<String> aliases = xmlConfig.getCacheConfigurations().keySet();
            Assert.assertFalse(aliases.isEmpty(), "No caches configured");
            try (CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig)) {
                cacheManager.init();
                Assert.assertEquals(cacheManager.getStatus(), Status.AVAILABLE);
                for (String alias : aliases) {
                    Cache<String, String> cache = cacheManager.getCache(alias, String.class, String.class);
                    Assert.assertNotNull(cache, "Cache not available: " + alias);
                    cache.put("key", "value");
                    Assert.assertEquals(cache.get("key"), "value", "Cache: " + alias);
                }
            }
        } finally {
            System.setProperty("java.io.tmpdir", originalTmpDir);
        }
    }
}
