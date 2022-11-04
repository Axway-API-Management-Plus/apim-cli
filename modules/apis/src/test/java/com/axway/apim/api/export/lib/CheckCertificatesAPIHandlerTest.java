package com.axway.apim.api.export.lib;

import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.CheckCertificatesAPIHandler;
import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CheckCertificatesAPIHandlerTest {
    private APICheckCertificatesParams apiExportParams = new APICheckCertificatesParams();


    public  List<API> setup(){

        API api = new API();
        api.setId("id-122");
        api.setName("Test API");
        api.setPath("/test");
        api.setVersion("1.0.0");
        CaCert caCert = new CaCert();
        caCert.setName("CN=apigateway");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -5);
        caCert.setNotValidBefore(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, 1);
        caCert.setNotValidAfter(calendar.getTimeInMillis());
        caCert.setMd5Fingerprint("121131313");
        List<CaCert> caCertList = new ArrayList<>();
        caCertList.add(caCert);
        api.setCaCerts(caCertList);
        List<API> apis = new ArrayList<>();
        apis.add(api);
        return apis;
    }

    @Test
    public void exportJsonTest() throws AppException {
        apiExportParams.setOutputFormat(StandardExportParams.OutputFormat.json);
        apiExportParams.setNumberOfDays(1);
        CheckCertificatesAPIHandler checkCertificatesAPIHandler = new CheckCertificatesAPIHandler(apiExportParams);
        List<API> apis = setup();
        checkCertificatesAPIHandler.execute(apis);
    }


    @Test
    public void consoleTest() throws AppException {
        apiExportParams.setNumberOfDays(1);
        CheckCertificatesAPIHandler checkCertificatesAPIHandler = new CheckCertificatesAPIHandler(apiExportParams);
        List<API> apis = setup();
        checkCertificatesAPIHandler.execute(apis);
    }
}
