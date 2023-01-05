package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"original"})
public class APIMethod {
    /**
     * The ID of the FE-API operation
     */
    private String id;
    private String virtualizedApiId;
    private String name;
    private String apiId;
    /**
     * The ID of the Backend-API method
     */
    private String apiMethodId;
    private String summary;
    private String original;
    private String descriptionManual;
    private String descriptionMarkdown;
    private String descriptionUrl;
    private String descriptionType;
    private TagMap tags = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVirtualizedApiId() {
        return virtualizedApiId;
    }

    public void setVirtualizedApiId(String virtualizedApiId) {
        this.virtualizedApiId = virtualizedApiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiMethodId() {
        return apiMethodId;
    }

    public void setApiMethodId(String apiMethodId) {
        this.apiMethodId = apiMethodId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getDescriptionManual() {
        return descriptionManual;
    }

    public void setDescriptionManual(String descriptionManual) {
        this.descriptionManual = descriptionManual;
    }

    public String getDescriptionMarkdown() {
        return descriptionMarkdown;
    }

    public void setDescriptionMarkdown(String descriptionMarkdown) {
        this.descriptionMarkdown = descriptionMarkdown;
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }

    public void setDescriptionUrl(String descriptionUrl) {
        this.descriptionUrl = descriptionUrl;
    }

    public TagMap getTags() {
        return tags;
    }

    public void setTags(TagMap tags) {
        this.tags = tags;
    }

    public String getDescriptionType() {
        return descriptionType;
    }

    public void setDescriptionType(String descriptionType) {
        this.descriptionType = descriptionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APIMethod apiMethod = (APIMethod) o;
        if (name.equals(apiMethod.name) && summary.equals(apiMethod.summary) && descriptionType.equals(apiMethod.descriptionType)) {
            boolean flag = tags == null && apiMethod.tags == null;
            if (tags != null && apiMethod.tags != null) {
                flag = tags.equals(apiMethod.tags);
            }
            if (descriptionType.equals("manual") && descriptionManual.equals(apiMethod.descriptionManual)) {
                flag = true;
            } else if (descriptionType.equals("url") && descriptionUrl.equals(apiMethod.descriptionUrl)) {
                flag = true;
            } else if (descriptionType.equals("markdown") && descriptionMarkdown.equals(apiMethod.descriptionMarkdown)) {
                flag = true;
            }
            return flag;
        }
        return false;
    }
}


