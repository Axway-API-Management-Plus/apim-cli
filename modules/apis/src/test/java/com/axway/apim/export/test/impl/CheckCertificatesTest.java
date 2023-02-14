package com.axway.apim.export.test.impl;

import com.axway.apim.APIManagerMockBase;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.CheckCertificatesAPIHandler;
import com.axway.apim.api.export.lib.cli.CLICheckCertificatesOptions;
import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class CheckCertificatesTest extends APIManagerMockBase {

    private static final String TEST_PACKAGE = "test/export/files/apiLists/";

    ObjectMapper mapper = new ObjectMapper();


    @BeforeClass
    public void setTest() throws  IOException {
        setupMockData();
        mapper.disable(MapperFeature.USE_ANNOTATIONS);
    }

    @Test
    public void checkCertNothingAboutToExpire() throws  IOException {
        List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<List<API>>() {
        });

        APICheckCertificatesParams params = (APICheckCertificatesParams) CLICheckCertificatesOptions.create(new String[]{"-days", "30"}).getParams();
        CheckCertificatesAPIHandler checkCerts = new CheckCertificatesAPIHandler(params);
        checkCerts.execute(apis);
        Result result = checkCerts.getResult();
        Assert.assertTrue(result.getErrorCode() == ErrorCode.SUCCESS);
    }

    @Test
    public void checkSomeExpiredCerts() throws IOException {
        List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<List<API>>() {
        });

        APICheckCertificatesParams params = (APICheckCertificatesParams) CLICheckCertificatesOptions.create(new String[]{"-days", "23350"}).getParams();
        CheckCertificatesAPIHandler checkCerts = new CheckCertificatesAPIHandler(params);
        checkCerts.execute(apis);
        Result result = checkCerts.getResult();
        Assert.assertTrue(result.getErrorCode() == ErrorCode.CHECK_CERTS_FOUND_CERTS);
        @SuppressWarnings("unchecked")
        List<CaCert> expiredCert = (List<CaCert>) result.getResultDetails();
        Assert.assertTrue(expiredCert.size() == 1, "Expect one certificate to expire");
    }
}