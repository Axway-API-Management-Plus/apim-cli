package com.axway.apim.lib.utils;

public final class Constants {

    private Constants() {
       throw new IllegalStateException("Object creation is not allowed");
    }
    public static final String API_UNPUBLISHED = "unpublished";
    public static final String API_PUBLISHED = "published";
    public static final String API_DEPRECATED = "deprecated";
    public static final String API_DELETED = "deleted";
    public static final String API_UNDEPRECATED = "undeprecated";
    public static final String API_PENDING = "pending";
}
