package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.model.APICert;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CheckCertificatesAPIHandler extends APIResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CheckCertificatesAPIHandler.class);
    APICheckCertificatesParams checkCertParams;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public CheckCertificatesAPIHandler(APIExportParams params) {
        super(params);
        this.checkCertParams = (APICheckCertificatesParams) params;
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        cal.add(Calendar.DAY_OF_YEAR, checkCertParams.getNumberOfDays());
        if (LOG.isDebugEnabled())
            LOG.debug("Going to check certificate expiration of: {} selected API(s) within the next {} days (Not valid after: {})", apis.size(), checkCertParams.getNumberOfDays(), formatDate(cal.getTime().getTime()));
        List<ApiPlusCert> expiredCerts = getExpiredCerts(apis);
        if (!expiredCerts.isEmpty()) {
            this.result.setError(ErrorCode.CHECK_CERTS_FOUND_CERTS);
            this.result.setResultDetails(expiredCerts);
            StandardExportParams.OutputFormat outputFormat = params.getOutputFormat();
            if (outputFormat.equals(StandardExportParams.OutputFormat.console)) {
                Console.println("The following certificates will expire in the next " + checkCertParams.getNumberOfDays() + " days.");
                Console.println(AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS, expiredCerts, Arrays.asList(
                    new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getId()),
                    new Column().header("API-Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getName()),
                    new Column().header("API-Path").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getPath()),
                    new Column().header("API-Ver.").with(expired -> expired.api.getVersion()),
                    new Column().header("Certificate-Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.certificate.getName()),
                    new Column().header("Not valid after").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> formatDate(expired.certificate.getNotValidAfter())),
                    new Column().header("Not valid before").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> formatDate(expired.certificate.getNotValidBefore())),
                    new Column().header("MD5-Fingerprint").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.certificate.getMd5Fingerprint())
                )));
            } else if (outputFormat.equals(StandardExportParams.OutputFormat.json)) {
                List<APICert> apiCerts = getApiCerts(expiredCerts);
                writeJSON(apiCerts);
            }
        } else {
            LOG.info("No certificates found that will expire within the next {} days.", checkCertParams.getNumberOfDays());
        }
        LOG.info("Done!");
    }

    public List<ApiPlusCert> getExpiredCerts(List<API> apis){
        List<ApiPlusCert> expiredCerts = new ArrayList<>();
        for (API api : apis) {
            if (api.getCaCerts() == null) continue;
            List<CaCert> certificates = api.getCaCerts();
            for (CaCert certificate : certificates) {
                try {
                    Date notValidAfter = new Date(certificate.getNotValidAfter());
                    if (notValidAfter.before(cal.getTime())) {
                        expiredCerts.add(new ApiPlusCert(api, certificate));
                    }
                } catch (Exception e) {
                    LOG.error("Error checking certificate: {} expiration date used by API: {}", certificate.getAlias(), api.toStringHuman(), e);
                    this.result.setError(ErrorCode.CHECK_CERTS_UNXPECTED_ERROR);
                }
            }
        }
        return expiredCerts;
    }

    private static List<APICert> getApiCerts(List<ApiPlusCert> expiredCerts) {
        List<APICert> apiCerts = new ArrayList<>();
        for (ApiPlusCert apiPlusCert : expiredCerts) {
            String id = apiPlusCert.api.getId();
            String apiName = apiPlusCert.api.getName();
            String path = apiPlusCert.api.getPath();
            String version = apiPlusCert.api.getVersion();
            String commonName = apiPlusCert.certificate.getName();
            long validAfter = apiPlusCert.certificate.getNotValidAfter();
            long validBefore = apiPlusCert.certificate.getNotValidBefore();
            String md5 = apiPlusCert.certificate.getMd5Fingerprint();
            APICert apiCert = new APICert(id, apiName, path, version, commonName, validAfter, validBefore, md5);
            apiCerts.add(apiCert);
        }
        return apiCerts;
    }

    public void writeJSON(List<APICert> apiCerts) throws AppException {
        try {
            String folderName = "certs";
            String givenTarget = params.getTarget();
            File localFolder = new File(givenTarget + File.separator + folderName);
            LOG.debug("Going to export expired certificates details into folder: {}", localFolder);
            exportHelper.validateFolder(localFolder);
            String filePath = localFolder.getCanonicalPath() + File.separator + "certificates.json";
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            try (Writer writer = new FileWriter(filePath)) {
                mapper.writeValue(writer, apiCerts);
            }
            LOG.debug("Successfully exported Certificate Expiry Data to file : {}", filePath);
        } catch (IOException e) {
            throw new AppException("Error writing json", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder();
        return builder.build();
    }

    public static class ApiPlusCert {
        CaCert certificate;
        API api;

        public ApiPlusCert(API api, CaCert certificate) {
            this.certificate = certificate;
            this.api = api;
        }
    }

    private String formatDate(Long date) {
        return dateFormatter.format(new Date(date));
    }
}
