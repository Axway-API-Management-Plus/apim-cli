package com.axway.apim.adapter.apis;

public enum StatusEndpoint {
    UNPUBLISHED("unpublish"),
    PUBLISHED("publish"),
    DEPRECATED("deprecate"),
    UNDEPRECATED("undeprecate");

    final String endpoint;

    StatusEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}