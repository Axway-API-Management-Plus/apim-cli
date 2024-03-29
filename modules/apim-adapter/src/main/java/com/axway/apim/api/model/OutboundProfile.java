package com.axway.apim.api.model;

import com.axway.apim.adapter.jackson.PolicyDeserializer;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OutboundProfile extends Profile {

    String routeType;

    @JsonDeserialize(using = PolicyDeserializer.class)
    Policy requestPolicy;

    @JsonDeserialize(using = PolicyDeserializer.class)
    Policy responsePolicy;

    @JsonDeserialize(using = PolicyDeserializer.class)
    Policy routePolicy;

    @JsonDeserialize(using = PolicyDeserializer.class)
    Policy faultHandlerPolicy;

    String authenticationProfile;

    List<Object> parameters = new ArrayList<>();


    public String getAuthenticationProfile() {
        // give a default value in case of blank value
        // useful in equals methods null = "" = _default
        if (StringUtils.isBlank(authenticationProfile))
            return "_default";
        else
            return this.authenticationProfile;
    }

    public void setAuthenticationProfile(String authenticationProfile) {
        this.authenticationProfile = authenticationProfile;
    }

    public String getRouteType() {
        // default value policy is set in case of an existing value (different of "proxy" ) or in case of existing routePolicy
        if ((StringUtils.isNotBlank(routeType) && !StringUtils.equals("proxy", routeType)) || (routePolicy != null)) {
            return "policy";
        } else {
            return "proxy";
        }
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public Policy getRequestPolicy() {
        return requestPolicy;
    }

    public void setRequestPolicy(Policy requestPolicy) {
        this.requestPolicy = requestPolicy;
    }

    public Policy getResponsePolicy() {
        return responsePolicy;
    }

    public void setResponsePolicy(Policy responsePolicy) {
        this.responsePolicy = responsePolicy;
    }

    public Policy getRoutePolicy() {
        return routePolicy;
    }

    public void setRoutePolicy(Policy routePolicy) {
        this.routePolicy = routePolicy;
    }

    public Policy getFaultHandlerPolicy() {
        return faultHandlerPolicy;
    }

    public void setFaultHandlerPolicy(Policy faultHandlerPolicy) {
        this.faultHandlerPolicy = faultHandlerPolicy;
    }

    public List<Object> getParameters() {
        if (parameters == null)
            return Collections.emptyList();
        return parameters;
    }

    @JsonIgnore
    public List<Policy> getAllPolices() {
        List<Policy> usedPolicies = new ArrayList<>();
        if (this.requestPolicy != null)
            usedPolicies.add(this.requestPolicy);
        if (this.routePolicy != null)
            usedPolicies.add(this.routePolicy);
        if (this.responsePolicy != null)
            usedPolicies.add(this.responsePolicy);
        if (this.faultHandlerPolicy != null)
            usedPolicies.add(this.faultHandlerPolicy);
        return usedPolicies;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other instanceof OutboundProfile) {
            OutboundProfile otherOutboundProfile = (OutboundProfile) other;
            List<Object> otherParameters = otherOutboundProfile.getParameters();
            List<Object> thisParameters = this.getParameters();
            return policiesAreEqual(this.getFaultHandlerPolicy(), otherOutboundProfile.getFaultHandlerPolicy())
                && policiesAreEqual(this.getRequestPolicy(), otherOutboundProfile.getRequestPolicy())
                && policiesAreEqual(this.getResponsePolicy(), otherOutboundProfile.getResponsePolicy())
                && policiesAreEqual(this.getRoutePolicy(), otherOutboundProfile.getRoutePolicy())
                && StringUtils.equalsIgnoreCase(this.getRouteType(), otherOutboundProfile.getRouteType())
                && StringUtils.equalsIgnoreCase(this.getAuthenticationProfile(),
                otherOutboundProfile.getAuthenticationProfile())
                && Utils.compareValues(thisParameters, otherParameters);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeType, requestPolicy, responsePolicy, routePolicy, faultHandlerPolicy, authenticationProfile, parameters);
    }

    @Override
    public String toString() {
        return "OutboundProfile{" +
            "routeType='" + routeType + '\'' +
            ", requestPolicy=" + requestPolicy +
            ", responsePolicy=" + responsePolicy +
            ", routePolicy=" + routePolicy +
            ", faultHandlerPolicy=" + faultHandlerPolicy +
            ", authenticationProfile='" + authenticationProfile + '\'' +
            ", parameters=" + parameters +
            '}';
    }

    private boolean policiesAreEqual(Policy policyA, Policy policyB) {
        if (policyA == null && policyB == null)
            return true;
        return (policyA != null && policyA.equals(policyB));
    }
}
