package com.axway.apim.adapter.apis;

public enum StatusEndpoint {
    unpublished("unpublish"),
    published("publish"),
    deprecated("deprecate"),
    undeprecated("undeprecate");

    final String endpoint;

    StatusEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}