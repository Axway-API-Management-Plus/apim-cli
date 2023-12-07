package com.axway.apim.api.export.model;

public class APICert {

    private final String id;
    private final String name;
    private final String path;
    private final String version;
    private final String commonName;
    private final long validAfter;
    private final long validBefore;
    private final String md5FingerPrint;

    public APICert(String id, String name, String path, String version, String commonName, long validAfter, long validBefore, String md5FingerPrint) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.version = version;
        this.commonName = commonName;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
        this.md5FingerPrint = md5FingerPrint;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getPath() {
        return path;
    }


    public String getVersion() {
        return version;
    }

    public String getCommonName() {
        return commonName;
    }


    public long getValidAfter() {
        return validAfter;
    }


    public long getValidBefore() {
        return validBefore;
    }


    public String getMd5FingerPrint() {
        return md5FingerPrint;
    }

}
