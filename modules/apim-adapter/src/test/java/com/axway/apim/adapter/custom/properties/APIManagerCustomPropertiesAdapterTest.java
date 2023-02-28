package com.axway.apim.adapter.custom.properties;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.custom.properties.APIManagerCustomPropertiesAdapter;
import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class APIManagerCustomPropertiesAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apimanagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apimanagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void getCustomProperties() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        CustomProperties customProperties = apiManagerCustomPropertiesAdapter.getCustomProperties();
        Assert.assertNotNull(customProperties);
        Assert.assertNotNull(customProperties.getApi());
        Assert.assertNotNull(customProperties.getApplication());
        Assert.assertNotNull(customProperties.getOrganization());
        Assert.assertNotNull(customProperties.getUser());
    }

    @Test
    public void getCustomPropertiesApi() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        Map<String, CustomProperty> customPropertyMap = apiManagerCustomPropertiesAdapter.getCustomProperties(CustomProperties.Type.api);
        Assert.assertNotNull(customPropertyMap);
    }

    @Test
    public void getCustomPropertiesApplication() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        Map<String, CustomProperty> customPropertyMap = apiManagerCustomPropertiesAdapter.getCustomProperties(CustomProperties.Type.application);
        Assert.assertNotNull(customPropertyMap);
    }

    @Test
    public void getCustomPropertiesOrganization() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        Map<String, CustomProperty> customPropertyMap = apiManagerCustomPropertiesAdapter.getCustomProperties(CustomProperties.Type.organization);
        Assert.assertNotNull(customPropertyMap);
    }

    @Test
    public void getCustomPropertiesUser() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        Map<String, CustomProperty> customPropertyMap = apiManagerCustomPropertiesAdapter.getCustomProperties(CustomProperties.Type.user);
        Assert.assertNotNull(customPropertyMap);
    }

    @Test
    public void getCustomPropertyNames() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        List<String> names = apiManagerCustomPropertiesAdapter.getCustomPropertyNames(CustomProperties.Type.api);
        Assert.assertNotNull(names);
    }

    @Test
    public void getRequiredCustomProperties() throws AppException {
        APIManagerCustomPropertiesAdapter apiManagerCustomPropertiesAdapter = apimanagerAdapter.customPropertiesAdapter;
        Map<String, CustomProperty> customPropertyMap = apiManagerCustomPropertiesAdapter.getRequiredCustomProperties(CustomProperties.Type.api);
        Assert.assertNotNull(customPropertyMap);
    }
}
