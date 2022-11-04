package com.axway.apim.api.model;

public class APICert {

    String id;
    String name;
    String path;
    String version;
    String commonName;
    long validAfter;
    long validBefore;
    String md5FingerPrint;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public long getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(long validAfter) {
        this.validAfter = validAfter;
    }

    public long getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(long validBefore) {
        this.validBefore = validBefore;
    }

    public String getMd5FingerPrint() {
        return md5FingerPrint;
    }

    public void setMd5FingerPrint(String md5FingerPrint) {
        this.md5FingerPrint = md5FingerPrint;
    }
}
