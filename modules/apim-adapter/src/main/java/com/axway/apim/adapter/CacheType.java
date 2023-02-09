package com.axway.apim.adapter;

public enum CacheType {
    applicationAPIAccessCache,
    organizationAPIAccessCache,
    oauthClientProviderCache,
    applicationsCache,
    applicationsSubscriptionCache,
    applicationsQuotaCache(true),
    applicationsCredentialCache,
    organizationCache,
    userCache;

    public final boolean supportsImportActions;

    CacheType() {
        this.supportsImportActions = false;
    }

    CacheType(boolean supportsImportActions) {
        this.supportsImportActions = supportsImportActions;
    }
}