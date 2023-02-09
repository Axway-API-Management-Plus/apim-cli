package com.axway.apim.appexport;

import com.axway.apim.WiremockWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ApplicationExportAppTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void exportApplication() {
        String[] args = {"-h", "localhost", "-name", "Test App 2008"};
        int returnCode = ApplicationExportApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportInvalidApplication() {
        String[] args = {"-h", "localhost", "-name", "Test App 2008 invalid"};
        int returnCode = ApplicationExportApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportApplications() {
        String[] args = {"-h", "localhost"};
        int returnCode = ApplicationExportApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteApplication() {
        String[] args = {"-h", "localhost", "-name", "Test App 2008"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = ApplicationExportApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteApplicationInvalid() {
        String[] args = {"-h", "localhost", "-name", "Test App 2008 Invalid"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = ApplicationExportApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteApplications() {
        String[] args = {"-h", "localhost"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = ApplicationExportApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }
}
