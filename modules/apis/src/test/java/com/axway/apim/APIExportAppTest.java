package com.axway.apim;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

public class APIExportAppTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void testExportAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testExportAPIWithId() {
        String[] args = {"-h", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testExportApiCsv() {
        String[] args = {"-h", "localhost", "-o", "csv", "-deleteTarget"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);

    }

    @Test
    public void testExportApiJson() {
        String[] args = {"-h", "localhost", "-o", "json", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-deleteTarget"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);

    }

    @Test
    public void testExportApiYaml() {
        String[] args = {"-h", "localhost", "-o", "yaml", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-deleteTarget"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);

    }

    @Test
    public void testExportApiDat() {
        String[] args = {"-h", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-o", "dat", "-deleteTarget"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testExportDefault() {
        String[] args = {"-h", "localhost", "-deleteTarget"};
        int returnCode = APIExportApp.exportAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testUnPublishAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.unpublish(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testPublishAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.publish(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testDeleteAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }


    @Test
    public void testApproveAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.approve(args);
        Assert.assertEquals(returnCode, 0);
    }


    @Test
    public void testChange() {
        String[] args = {"-h", "localhost", "-n", "petstore", "-newBackend", "https://api.axway.com", "-force"};
        int returnCode = APIExportApp.change(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testUpgradeAccessAPIWithName() {
        // "-refAPIRetireDate", "31.12.2023"
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        String[] args = {"-h", "localhost", "-n", "petstore", "-refAPIRetireDate", calendar.get(Calendar.DATE) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR)};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.upgradeAccess(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testGrantAccessAPIWithName() {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = APIExportApp.grantAccess(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void testCheckCertificates() {
        String[] args = {"-h", "localhost", "-n", "petstore", "-days", "1000"};
        int returnCode = APIExportApp.checkCertificates(args);
        Assert.assertEquals(returnCode, 101);
    }

    @Test
    public void testGetName(){
        APIExportApp apiExportApp = new APIExportApp();
        Assert.assertEquals(apiExportApp.getName(), "API - E X P O R T / U T I L S");
    }

    @Test
    public void testGetGroupDescription(){
        APIExportApp apiExportApp = new APIExportApp();
        Assert.assertEquals(apiExportApp.getGroupDescription(), "Manage your APIs");
    }

    @Test
    public void testGroupId(){
        APIExportApp apiExportApp = new APIExportApp();
        Assert.assertEquals(apiExportApp.getGroupId(), "api");
    }


    @Test
    public void testApi() {
        String[] args = {"-h", "10.133.132.116", "-n", "dev_calponia.yaml_2023-04-17T13 34 51Z.yaml", "-o", "json", "-deleteTarget"};

        int returnCode = APIExportApp.exportAPI(args);
        System.out.println(returnCode);

    }

}
