package com.axway.apim;

import com.axway.apim.api.API;
import com.axway.apim.apiimport.lib.cli.CLIAPIImportOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;

public class APIImportAppTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void importApiTest() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specFile = classLoader.getResource("api_definition_1/petstore-openapi30.json").getFile();
        String confFile = classLoader.getResource("com/axway/apim/test/files/basic/config.json").getFile();
        String[] args = {"-h", "localhost", "-c", confFile, "-a", specFile};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 15);
    }

    @Test
    public void importApiWithReturnCodeMapping() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specFile = classLoader.getResource("api_definition_1/petstore-openapi30.json").getFile();
        String confFile = classLoader.getResource("com/axway/apim/test/files/basic/config.json").getFile();
        // Same as importApiTest, which results in 15 (breaking change). Mapped to 0 using the return code mapping.
        String[] args = {"-h", "localhost", "-c", confFile, "-a", specFile, "-returnCodeMapping", "15:0"};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0, "The returnCodeMapping must be applied to the return code");
    }

    private APIImportParams breakingChangeParams(String... additionalArgs) throws AppException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specFile = classLoader.getResource("api_definition_1/petstore-openapi30.json").getFile();
        String confFile = classLoader.getResource("com/axway/apim/test/files/basic/config.json").getFile();
        List<String> args = new ArrayList<>(Arrays.asList("-h", "localhost", "-c", confFile, "-a", specFile));
        args.addAll(Arrays.asList(additionalArgs));
        return (APIImportParams) CLIAPIImportOptions.create(args.toArray(new String[0])).getParams();
    }

    private String captureStdErr(Runnable runnable) {
        PrintStream original = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            runnable.run();
        } finally {
            System.setErr(original);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Wiremock writes its own diagnostics to stderr, hence only the lines with the error details are taken.
     */
    private List<String> errorDetailLines(String stdErr) {
        List<String> lines = new ArrayList<>();
        for (String line : stdErr.split("\\R")) {
            if (line.startsWith("{\"errorCode\"")) lines.add(line);
        }
        return lines;
    }

    // Issue #594: Return the details of an error
    @Test
    public void errorDetailsArePrintedIfEnabled() throws Exception {
        APIImportApp app = new APIImportApp();
        app.setPrintErrorDetails(true);
        APIImportParams params = breakingChangeParams("-returnCodeMapping", "15:20");
        int[] returnCode = new int[1];
        String stdErr = captureStdErr(() -> returnCode[0] = app.importAPI(params));

        Assert.assertEquals(returnCode[0], 20, "Return code must be mapped");
        List<String> lines = errorDetailLines(stdErr);
        Assert.assertEquals(lines.size(), 1, "Expected exactly one line with the error details. stderr: " + stdErr);
        JsonNode details = new ObjectMapper().readTree(lines.get(0));
        Assert.assertEquals(details.get("errorCode").asInt(), 15);
        Assert.assertEquals(details.get("error").asText(), "BREAKING_CHANGE_DETECTED");
        Assert.assertEquals(details.get("mappedErrorCode").asInt(), 20);
        Assert.assertFalse(details.get("message").asText().isEmpty());
        Assert.assertTrue(details.get("details").isArray());
    }

    @Test
    public void errorDetailsAreNotPrintedByDefault() throws Exception {
        APIImportApp app = new APIImportApp();
        app.setPrintErrorDetails(false);
        APIImportParams params = breakingChangeParams();
        int[] returnCode = new int[1];
        String stdErr = captureStdErr(() -> returnCode[0] = app.importAPI(params));

        Assert.assertEquals(returnCode[0], 15);
        Assert.assertTrue(errorDetailLines(stdErr).isEmpty(), "The error details must not be printed if not enabled. stderr: " + stdErr);
    }

    @Test
    public void importAPIwithDetailsReturnsTheDetailsOfAnError() throws Exception {
        String result = new APIImportApp().importAPIwithDetails(breakingChangeParams("-returnCodeMapping", "15:0"));
        JsonNode details = new ObjectMapper().readTree(result);
        Assert.assertEquals(details.get("errorCode").asInt(), 15);
        Assert.assertEquals(details.get("error").asText(), "BREAKING_CHANGE_DETECTED");
        Assert.assertEquals(details.get("mappedErrorCode").asInt(), 0, "The returnCodeMapping must be applied");
    }

    @Test
    public void importApiMCliHelp() {
        String[] args = {"-help"};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importApiMCliVersion() {
        String[] args = {"-version"};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testEmptyPassword() throws Exception {
        APIImportParams params = new APIImportParams();
        ClassLoader classLoader = this.getClass().getClassLoader();
        String confFile = classLoader.getResource("com/axway/apim/test/files/serviceprofile/empty_password.json").getFile();
        params.setConfig(confFile);
        params.setUsername("admin");
        params.setPassword("admin");
        params.setAPIManagerURL("https://localhost:8075");
        APIImportConfigAdapter configAdapter = new APIImportConfigAdapter(params);
        // Creates an API-Representation of the desired API
        API desiredAPI = configAdapter.getDesiredAPI();
        Assert.assertEquals("",desiredAPI.getAuthenticationProfiles().get(0).getParameters().get("password"));

    }

    @Test
    public void safeUpdateAllowsSameName() throws AppException {
        APIImportApp app = new APIImportApp();
        APIImportParams params = new APIImportParams();
        params.setSafeUpdate(true);

        API actualApi = new API();
        actualApi.setId("actual-id");
        actualApi.setName("Order API");

        API desiredApi = new API();
        desiredApi.setId("desired-id");
        desiredApi.setName("Order API");
        desiredApi.setPath("/api/orders/v1");

        app.validateSafeUpdate(actualApi, desiredApi, params);
    }

    @Test
    public void safeUpdateAllowsNameMismatchWhenApiIdMatches() throws AppException {
        APIImportApp app = new APIImportApp();
        APIImportParams params = new APIImportParams();
        params.setSafeUpdate(true);

        API actualApi = new API();
        actualApi.setId("same-id");
        actualApi.setName("Order API");

        API desiredApi = new API();
        desiredApi.setId("same-id");
        desiredApi.setName("Inventory API");
        desiredApi.setPath("/api/orders/v1");

        app.validateSafeUpdate(actualApi, desiredApi, params);
    }

    @Test
    public void safeUpdateBlocksNameMismatchForSamePath() {
        APIImportApp app = new APIImportApp();
        APIImportParams params = new APIImportParams();
        params.setSafeUpdate(true);

        API actualApi = new API();
        actualApi.setId("actual-id");
        actualApi.setName("Order API");

        API desiredApi = new API();
        desiredApi.setId("different-id");
        desiredApi.setName("Inventory API");
        desiredApi.setPath("/api/orders/v1");

        AppException exception = Assert.expectThrows(AppException.class, () -> app.validateSafeUpdate(actualApi, desiredApi, params));
        Assert.assertEquals(exception.getError(), ErrorCode.API_PATH_IN_USE_BY_DIFFERENT_API);
    }
}
