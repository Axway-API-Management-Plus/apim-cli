package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

public class APISpecificationFilter {

    private List<APISpecIncludeExcludeFilter> include = new ArrayList<>();
    private List<APISpecIncludeExcludeFilter> exclude = new ArrayList<>();

    public List<APISpecIncludeExcludeFilter> getInclude() {
        return include;
    }

    public void setInclude(List<APISpecIncludeExcludeFilter> include) {
        this.include = include;
    }

    public List<APISpecIncludeExcludeFilter> getExclude() {
        return exclude;
    }

    public void setExclude(List<APISpecIncludeExcludeFilter> exclude) {
        this.exclude = exclude;
    }

    /**
     * This method is for tests only
     *
     * @param pathAndVerbs an array of paths and verbs to include
     * @param tags         an array of tags to include
     */
    public void addInclude(String[] pathAndVerbs, String[] tags) {
        addInclude(pathAndVerbs, tags, null);
    }

    /**
     * This method is for tests only
     *
     * @param pathAndVerbs an array of paths and verbs to exclude
     * @param tags         an array of tags to exclude
     */
    public void addExclude(String[] pathAndVerbs, String[] tags) {
        addExclude(pathAndVerbs, tags, null);
    }


    /**
     * This method is for tests only
     *
     * @param pathAndVerbs an array of paths and verbs to include
     * @param tags         an array of tags to include
     * @param models       an array of models to include
     */
    public void addInclude(String[] pathAndVerbs, String[] tags, String[] models) {
        APISpecIncludeExcludeFilter filter = new APISpecIncludeExcludeFilter();
        if (pathAndVerbs != null) filter.addPath(pathAndVerbs);
        if (tags != null) filter.addTag(tags);
        if (models != null) filter.addModel(models);
        this.include.add(filter);
    }

    /**
     * This method is for tests only
     *
     * @param pathAndVerbs an array of paths and verbs to exclude
     * @param tags         an array of tags to exclude
     * @param models       an array of models to exclude
     */
    public void addExclude(String[] pathAndVerbs, String[] tags, String[] models) {
        APISpecIncludeExcludeFilter filter = new APISpecIncludeExcludeFilter();
        if (pathAndVerbs != null) filter.addPath(pathAndVerbs);
        if (tags != null) filter.addTag(tags);
        if (models != null) filter.addModel(models);
        this.exclude.add(filter);
    }
}
