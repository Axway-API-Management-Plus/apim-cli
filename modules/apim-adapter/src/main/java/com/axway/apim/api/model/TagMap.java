package com.axway.apim.api.model;

import java.util.LinkedHashMap;
import java.util.Objects;

public class TagMap extends LinkedHashMap<String, String[]> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public TagMap() {
        super();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TagMap)) return false;
        TagMap otherTagMap = (TagMap) o;
        if (otherTagMap.size() != size()) return false;
        for (String tagName : this.keySet()) {
            if (!otherTagMap.containsKey(tagName)) return false;
            String[] myTags = this.get(tagName);
            String[] otherTags = otherTagMap.get(tagName);
            if (!Objects.deepEquals(myTags, otherTags)) return false;
        }
        return true;
    }

}
