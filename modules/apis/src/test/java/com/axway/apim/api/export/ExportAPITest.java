package com.axway.apim.api.export;

import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportAPITest {

    @Test
    public void getApiMethodsEmpty() {
        API api = new API();
        api.setApiMethods(null);
        ExportAPI exportAPI = new ExportAPI(api);
        List<APIMethod> apiMethods = exportAPI.getApiMethods();
        Assert.assertTrue(apiMethods.isEmpty());
    }

    @Test
    public void getApiMethodsManual() {
        API api = new API();
        APIMethod apiMethod = new APIMethod();
        TagMap apiManagerTags = new TagMap();
        String[] tagValue = {"dev"};
        apiManagerTags.put("stage", tagValue);
        apiMethod.setTags(apiManagerTags);
        apiMethod.setDescriptionType("manual");
        apiMethod.setDescriptionManual("manual desc");
        List<APIMethod> apiMethods = new ArrayList<>();
        apiMethods.add(apiMethod);
        api.setApiMethods(apiMethods);
        ExportAPI exportAPI = new ExportAPI(api);
        List<APIMethod> apiMethodsResults = exportAPI.getApiMethods();
        Assert.assertEquals(apiMethodsResults.size(), 1);
        Assert.assertEquals(apiMethodsResults.get(0).getDescriptionManual(), "manual desc");

    }

    @Test
    public void getApiMethodsUrl() {
        API api = new API();
        APIMethod apiMethod = new APIMethod();
        TagMap apiManagerTags = new TagMap();
        String[] tagValue = {"dev"};
        apiManagerTags.put("stage", tagValue);
        apiMethod.setTags(apiManagerTags);
        apiMethod.setDescriptionType("url");
        apiMethod.setDescriptionUrl("https://docs.axway.com");
        List<APIMethod> apiMethods = new ArrayList<>();
        apiMethods.add(apiMethod);
        api.setApiMethods(apiMethods);
        ExportAPI exportAPI = new ExportAPI(api);
        List<APIMethod> apiMethodsResults = exportAPI.getApiMethods();
        Assert.assertEquals(apiMethodsResults.size(), 1);
        Assert.assertEquals(apiMethodsResults.get(0).getDescriptionUrl(), "https://docs.axway.com");

    }

    @Test
    public void getApiMethodsMarkdown() {
        API api = new API();
        APIMethod apiMethod = new APIMethod();
        TagMap apiManagerTags = new TagMap();
        String[] tagValue = {"dev"};
        apiManagerTags.put("stage", tagValue);
        apiMethod.setTags(apiManagerTags);
        apiMethod.setDescriptionType("markdown");
        apiMethod.setDescriptionMarkdown("markdown");
        List<APIMethod> apiMethods = new ArrayList<>();
        apiMethods.add(apiMethod);
        api.setApiMethods(apiMethods);
        ExportAPI exportAPI = new ExportAPI(api);
        List<APIMethod> apiMethodsResults = exportAPI.getApiMethods();
        Assert.assertEquals(apiMethodsResults.size(), 1);
        Assert.assertEquals(apiMethodsResults.get(0).getDescriptionMarkdown(), "markdown");
    }

    @Test
    public void getOutboundProfilesEmpty() {
        API api = new API();
        api.setOutboundProfiles(new HashMap<>());
        ExportAPI exportAPI = new ExportAPI(api);
        Map<String, OutboundProfile> outboundProfileMap = exportAPI.getOutboundProfiles();
        Assert.assertTrue(outboundProfileMap.isEmpty());
    }

    @Test
    public void getOutboundProfilesNull() {
        API api = new API();
        api.setOutboundProfiles(null);
        ExportAPI exportAPI = new ExportAPI(api);
        Map<String, OutboundProfile> outboundProfileMap = exportAPI.getOutboundProfiles();
        Assert.assertTrue(outboundProfileMap.isEmpty());
    }

    @Test
    public void getOutboundProfilesDefault() {
        API api = new API();
        Map<String, OutboundProfile> outboundProfileMap = new HashMap<>();
        api.setOutboundProfiles(outboundProfileMap);
        OutboundProfile outboundProfile = new OutboundProfile();
        outboundProfile.setRouteType("proxy");
        outboundProfile.setAuthenticationProfile("_default");
        outboundProfileMap.put("_default", outboundProfile);
        ExportAPI exportAPI = new ExportAPI(api);
        Map<String, OutboundProfile> resultOutboundProfileMap = exportAPI.getOutboundProfiles();
        Assert.assertTrue(resultOutboundProfileMap.isEmpty());
    }

    @Test
    public void getSecurityProfilesEmpty() {
        API api = new API();
        List<SecurityProfile> securityProfiles = new ArrayList<>();
        SecurityProfile securityProfile = new SecurityProfile();
        securityProfile.setDevices(new ArrayList<>());
        securityProfiles.add(securityProfile);
        api.setSecurityProfiles(securityProfiles);
        ExportAPI exportAPI = new ExportAPI(api);
        List<SecurityProfile> resultSecurityProfiles = exportAPI.getSecurityProfiles();
        Assert.assertTrue(resultSecurityProfiles.isEmpty());
    }


    @Test
    public void getSecurityProfilesPassThrough() {
        API api = new API();
        List<SecurityProfile> securityProfiles = new ArrayList<>();
        SecurityProfile securityProfile = new SecurityProfile();
        List<SecurityDevice> securityDevices = new ArrayList<>();
        SecurityDevice securityDevice = new SecurityDevice();
        securityDevice.setType(DeviceType.passThrough);
        securityDevices.add(securityDevice);
        securityProfile.setDevices(securityDevices);
        securityProfiles.add(securityProfile);
        api.setSecurityProfiles(securityProfiles);
        ExportAPI exportAPI = new ExportAPI(api);
        List<SecurityProfile> resultSecurityProfiles = exportAPI.getSecurityProfiles();
        Assert.assertTrue(resultSecurityProfiles.isEmpty());
    }
}
