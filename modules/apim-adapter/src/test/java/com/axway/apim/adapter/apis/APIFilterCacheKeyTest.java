package com.axway.apim.adapter.apis;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * The API-Manager responses are cached per APIFilter. Filters that result in a different request to the API-Manager
 * must not share a cache entry, otherwise the lookup of an API returns the API of a previous lookup (issue #567).
 */
public class APIFilterCacheKeyTest {

    @Test
    public void differentApiPathsDontShareCacheEntry() {
        Map<APIFilter, String> cache = new HashMap<>();
        APIFilter vestigingsprofielen = new APIFilter.Builder().hasApiPath("/kvk/vestigingsprofielen/v1").build();
        APIFilter zoeken = new APIFilter.Builder().hasApiPath("/kvk/zoeken/v2").build();
        APIFilter basisprofielen = new APIFilter.Builder().hasApiPath("/kvk/basisprofielen/v1").build();
        cache.put(vestigingsprofielen, "vestigingsprofielen");

        Assert.assertNull(cache.get(zoeken), "A different API path must not return the cached response of another API");
        Assert.assertNull(cache.get(basisprofielen));
        cache.put(zoeken, "zoeken");
        cache.put(basisprofielen, "basisprofielen");

        Assert.assertEquals(cache.size(), 3);
        Assert.assertEquals(cache.get(new APIFilter.Builder().hasApiPath("/kvk/vestigingsprofielen/v1").build()), "vestigingsprofielen");
        Assert.assertEquals(cache.get(new APIFilter.Builder().hasApiPath("/kvk/zoeken/v2").build()), "zoeken");
        Assert.assertEquals(cache.get(new APIFilter.Builder().hasApiPath("/kvk/basisprofielen/v1").build()), "basisprofielen");
    }

    @Test
    public void sameRequestSharesCacheEntry() {
        APIFilter first = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();
        APIFilter second = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();
        Assert.assertEquals(first, second);
        Assert.assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentStatesDontShareCacheEntry() {
        APIFilter published = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasState("published").build();
        APIFilter unpublished = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasState("unpublished").build();
        Assert.assertNotEquals(published, unpublished);
    }

    @Test
    public void differentNamesAndIdsDontShareCacheEntry() {
        Assert.assertNotEquals(new APIFilter.Builder().hasName("Petstore").build(), new APIFilter.Builder().hasName("Other").build());
        Assert.assertNotEquals(new APIFilter.Builder().hasId("1").build(), new APIFilter.Builder().hasId("2").build());
        Assert.assertEquals(new APIFilter.Builder().hasId("1").build(), new APIFilter.Builder().hasId("1").build());
    }

    @Test
    public void apiPathAndNameAreDifferentRequests() {
        Assert.assertNotEquals(new APIFilter.Builder().hasApiPath("/api/v1/resource").build(), new APIFilter.Builder().hasName("/api/v1/resource").build());
    }
}
