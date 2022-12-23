package com.axway.apim.adapter;

public enum CacheType {
    APPLICATION_API_ACCESS_CACHE,
    ORGANIZATION_API_ACCESS_CACHE,
    oauthClientProviderCache,
    APPLICATIONS_CACHE,
    APPLICATIONS_SUBSCRIPTION_CACHE,
    APPLICATIONS_QUOTA_CACHE(true),
    APPLICATIONS_CREDENTIAL_CACHE,
    ORGANIZATION_CACHE,
    USER_CACHE;

    public boolean supportsImportActions;

    CacheType() {
        this.supportsImportActions = false;
    }

    CacheType(boolean supportsImportActions) {
        this.supportsImportActions = supportsImportActions;
    }
}