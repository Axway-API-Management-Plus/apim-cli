package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SecurityProfile {

    private String name;
    private boolean isDefault;
    private List<SecurityDevice> devices;

    public SecurityProfile() {
        super();
        this.devices = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<SecurityDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<SecurityDevice> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "SecurityProfile [" + devices + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof SecurityProfile) {
            SecurityProfile securityProfile = (SecurityProfile) other;
            return
                    StringUtils.equals(securityProfile.getName(), this.getName()) &&
                            securityProfile.getIsDefault() == this.getIsDefault() &&
                            securityProfile.getDevices().equals(this.getDevices());
        } else {
            return false;
        }
    }
}
