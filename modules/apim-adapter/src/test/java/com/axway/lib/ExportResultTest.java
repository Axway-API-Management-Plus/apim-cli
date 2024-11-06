package com.axway.lib;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.ErrorCode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ExportResultTest {

    @Test
    public void testExportResult() {
        ExportResult result = new ExportResult();
        result.addExportedFile("api.json");
        Assert.assertNotNull(result.getExportedFiles());
        Assert.assertEquals(result.getExportedFiles().size(), 1);
    }

    @Test
    public void testErrorCode() {
        ExportResult result = new ExportResult();
        result.setError(ErrorCode.NO_CHANGE);
        Assert.assertTrue(result.hasError());
        Assert.assertNotNull(result.getErrorCode());
        Assert.assertEquals(result.getErrorCode(), ErrorCode.NO_CHANGE);
    }

    @Test
    public void getRc() {
        ExportResult result = new ExportResult();
        result.setError(ErrorCode.NO_CHANGE);
        Assert.assertNotNull(result.getErrorCode());
        Assert.assertEquals(result.getRc(), ErrorCode.NO_CHANGE.getCode());
    }

    @Test
    public void testToString() {
        ExportResult result = new ExportResult();
        result.setError(ErrorCode.NO_CHANGE);
        Assert.assertNotNull(result);
    }

    @Test
    public void restResultDetails() {
        List<String> expiredCerts = new ArrayList<>();
        expiredCerts.add("dn=axway expired");
        ExportResult result = new ExportResult();
        result.setResultDetails(expiredCerts);
        Assert.assertNotNull(result.getResultDetails());
    }
}

