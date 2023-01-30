package com.axway.apim.export.test.applications;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.export.test.ExportTestAction;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.testng.Assert.*;

@Test
public class ApplicationExportTestIT extends TestNGCitrusTestRunner {

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ExportTestAction swaggerExport = new ExportTestAction();
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API including applications to export it afterwards");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/api/test/" + this.getClass().getSimpleName() + "-${apiNumber}");
        variable("apiName", this.getClass().getSimpleName() + "-${apiNumber}");
        variable("state", "published");
        variable("exportLocation", "citrus:systemProperty('java.io.tmpdir')");
        variable(ExportTestAction.EXPORT_API, "${apiPath}");

        // These are the folder and filenames generated by the export tool
        variable("exportFolder", "api-test-${apiName}");
        variable("exportAPIName", "${apiName}.json");

        // ############## Creating Test-Application 1 #################
        createVariable("app1Name", "Consuming Test App 1 ${orgNumber}");
        http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
                .payload("{\"name\":\"${app1Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
                .extractFromPayload("$.id", "consumingTestApp1Id")
                .extractFromPayload("$.name", "consumingTestApp1Name"));

        echo("####### Created Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######");

        echo("####### Importing the API including applications, which should exported in the second step #######");
        createVariable(ImportTestAction.API_DEFINITION, "/test/export/files/basic/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, "/test/export/files/applications/1_api-with-0-org-1-app.json");
        createVariable("consumingTestAppName", "${consumingTestApp1Name}");
        createVariable("expectedReturnCode", "0");
        swaggerImport.doExecute(context);

        echo("####### Export the API including applications from the API-Manager #######");
        createVariable("expectedReturnCode", "0");
        swaggerExport.doExecute(context);
        String exportedAPIConfigFile = context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/api-config.json";
        echo("####### Reading exported API-Config file: '" + exportedAPIConfigFile + "' #######");
        JsonNode exportedAPIConfig = mapper.readTree(Files.newInputStream(new File(exportedAPIConfigFile).toPath()));
        assertEquals(exportedAPIConfig.get("version").asText(), "1.0.1");
        assertEquals(exportedAPIConfig.get("organization").asText(), "API Development " + context.getVariable("orgNumber"));
        //assertEquals(exportedAPIConfig.get("backendBasepath").asText(), 	"https://petstore.swagger.io");
        assertEquals(exportedAPIConfig.get("state").asText(), "published");
        assertEquals(exportedAPIConfig.get("path").asText(), context.getVariable("apiPath"));
        assertEquals(exportedAPIConfig.get("name").asText(), context.getVariable("apiName"));
        assertEquals(exportedAPIConfig.get("caCerts").size(), 4);

        assertEquals(exportedAPIConfig.get("caCerts").get(0).get("certFile").asText(), "swagger.io.crt");
        assertFalse(exportedAPIConfig.get("caCerts").get(0).get("inbound").asBoolean());
        assertTrue(exportedAPIConfig.get("caCerts").get(0).get("outbound").asBoolean());

        List<ClientApplication> exportedApps = mapper.convertValue(exportedAPIConfig.get("applications"), new TypeReference<List<ClientApplication>>() {
        });
        assertEquals(exportedApps.size(), 1, "Number of exported apps not correct");
        ClientApplication app = exportedApps.get(0);
        assertTrue(app.getApiAccess() == null || app.getApiAccess().size() == 0, "Exported Apps should not contains API-Access");
        assertNull(app.getId(), "The ID of an application shouldn't be exported.");
        assertNull(app.getOrganization(), "The Org-ID of an application shouldn't be exported.");
        assertNull(app.getAppQuota(), "The application quota should not be exported. It's not supported by the export!");
        assertTrue(new File(context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/swagger.io.crt").exists(), "Certificate swagger.io.crt is missing");
        assertTrue(new File(context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/StarfieldServicesRootCertificateAuthority-G2.crt").exists(), "Certificate StarfieldServicesRootCertificateAuthority-G2.crt is missing");
        assertTrue(new File(context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/AmazonRootCA1.crt").exists(), "Certificate AmazonRootCA1.crt is missing");
        // assertTrue(new File(context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/Amazon.crt").exists(), "Certificate Amazon.crt is missing");
        assertTrue(new File(context.getVariable("exportLocation") + "/" + context.getVariable("exportFolder") + "/" + context.getVariable("exportAPIName")).exists(), "Exported Swagger-File is missing");
    }
}
