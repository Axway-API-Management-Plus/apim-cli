package com.axway.apim.apiimport.actions;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.APISpecificationFactory;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class RecreateToUpdateAPITest extends WiremockWrapper {

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
        RecreateToUpdateAPI recreateToUpdateAPI = new RecreateToUpdateAPI();
        API actualAPI = new API();
        actualAPI.setName("petstore");
        actualAPI.setOrganization(organization);
        actualAPI.setPath("/api/v3");
        actualAPI.setVersion("1.1");
        actualAPI.setDescriptionType("original");
        actualAPI.setSummary("Petstore api");
        actualAPI.setState("published");
        actualAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        actualAPI.setApplications(new ArrayList<>());
        actualAPI.setApiId("1f4263ca-7f03-41d9-9d34-9eff79d29bd8");

        DesiredAPI desiredAPI = new DesiredAPI();
        desiredAPI.setName("petstore");
        desiredAPI.setPath("/api/v3");
        desiredAPI.setVersion("1.1");
        desiredAPI.setOrganization(organization);
        desiredAPI.setDescriptionType("original");
        desiredAPI.setSummary("Petstore api update");
        desiredAPI.setApplications(new ArrayList<>());
        desiredAPI.setState("published");
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specDirPath = classLoader.getResource("api_definition_1/").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("petstore-openapi30.json", specDirPath, "petstore");
        desiredAPI.setApiDefinition(apiSpecification);
        APIChangeState apiChangeState = new APIChangeState(actualAPI, desiredAPI);
        recreateToUpdateAPI.execute(apiChangeState);
    }
}
