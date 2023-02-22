package com.axway.apim.apiimport.actions;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RepublishToUpdateAPITest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void testRepublishToUpdateApi() throws AppException {
        APIManagerAdapter.deleteInstance();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        Organization organization = APIManagerAdapter.getInstance().orgAdapter.getOrgForName("orga");
        RepublishToUpdateAPI republishToUpdateAPI = new RepublishToUpdateAPI();
        API actualAPI = new API();
        actualAPI.setName("petstore");
        actualAPI.setOrganization(organization);
        actualAPI.setPath("/api/v3");
        actualAPI.setVersion("1.1");
        actualAPI.setDescriptionType("original");
        actualAPI.setSummary("Petstore api");
        actualAPI.setState("published");
        actualAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        API desiredAPI = new API();
        desiredAPI.setName("petstore");
        desiredAPI.setPath("/api/v3");
        desiredAPI.setVersion("1.1");
        desiredAPI.setOrganization(organization);
        desiredAPI.setDescriptionType("original");
        desiredAPI.setSummary("Petstore api update");
        desiredAPI.setState("published");
        APIChangeState apiChangeState = new APIChangeState(actualAPI, desiredAPI);
        republishToUpdateAPI.execute(apiChangeState);
    }
}
