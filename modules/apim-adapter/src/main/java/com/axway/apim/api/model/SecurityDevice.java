package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SecurityDevice {


    private String name;
    private DeviceType type;
    int order;
    private Map<String, String> properties = new HashMap<>();

    /**
     * Flag to control if Policy-Names should be translated or not - Currently used by the API-Export
     */
    @JsonIgnore
    boolean convertPolicies = true;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "SecurityDevice{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", order=" + order +
            ", properties=" + properties +
            ", convertPolicies=" + convertPolicies +
            '}';
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public void setConvertPolicies(boolean convertPolicies) {
        this.convertPolicies = convertPolicies;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof SecurityDevice) {
            SecurityDevice otherSecurityDevice = (SecurityDevice) other;
            if (!StringUtils.equals(otherSecurityDevice.getName(), this.getName())) return false;
            if (!StringUtils.equals(otherSecurityDevice.getType().getName(), this.getType().getName())) return false;
            //Ignore order check as 7.7.20211130 returning order id as  1 n whereas 7.7.20220830 returning order id as 0
            return otherSecurityDevice.getProperties().equals(this.getProperties());
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, properties);
    }
}
