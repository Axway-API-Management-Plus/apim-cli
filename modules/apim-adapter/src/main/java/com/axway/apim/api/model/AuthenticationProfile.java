package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthenticationProfile {

    public static final String PASSWORD = "password";
    private String name;

    private boolean isDefault;

    private Map<String, Object> parameters;

    private AuthType type;

    public AuthenticationProfile() {
        super();
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

    public Map<String, Object> getParameters() {
        if (parameters == null) return new HashMap<>();
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public AuthType getType() {
        return type;
    }

    public void setType(AuthType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other instanceof AuthenticationProfile) {
            AuthenticationProfile authenticationProfile = (AuthenticationProfile) other;
            Map<String, Object> otherParameters = authenticationProfile.getParameters();
            Map<String, Object> thisParameters = this.getParameters();
            otherParameters.remove("_id_");
            thisParameters.remove("_id_");
            if (StringUtils.equals(authenticationProfile.getName(), this.getName())
                && authenticationProfile.getIsDefault() == this.getIsDefault()
                && StringUtils.equals(authenticationProfile.getType().name(), this.getType().name())) {
                if (authenticationProfile.getType().equals(AuthType.ssl) || authenticationProfile.getType().equals(AuthType.http_basic)) {
                    Map<String, Object> otherParametersCopy = new HashMap<>(otherParameters);
                    Map<String, Object> thisParametersCopy = new HashMap<>(thisParameters);
                    // Passwords are no longer exposed by API-Manager REST-API - Can't use it anymore to compare the state, but can be overridden by setting
                    // system property com.axway.apimanager.api.model.disable.confidential.fields=false in API Gateway
                    if (otherParametersCopy.get(PASSWORD) == null || thisParametersCopy.get(PASSWORD) == null) {
                        otherParametersCopy.remove(PASSWORD);
                        thisParametersCopy.remove(PASSWORD);
                    }
                    return otherParametersCopy.equals(thisParametersCopy);
                } else {
                    return otherParameters.equals(thisParameters);
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String parametersString = this.getParameters().toString();
        if (type.equals(AuthType.ssl)) {
            String pfx = (String) parameters.get("pfx");
            if (pfx.length() > 50) pfx = pfx.substring(0, 49) + "...";
            parametersString = "{trustAll=" + this.getParameters().get("trustAll") + ", password=" + Utils.getEncryptedPassword() + ", pfx=" + pfx + "}";
        }
        return "AuthenticationProfile [name=" + name + ", isDefault=" + isDefault + ", parameters=" + parametersString
            + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isDefault, type);
    }
}
