package com.axway.apim.api.export.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class CheckCertificatesAPIHandler extends APIResultHandler {
	
	APICheckCertificatesParams checkCertParams;
	Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
	DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public CheckCertificatesAPIHandler(APIExportParams params) {
		super(params);
		this.checkCertParams = (APICheckCertificatesParams)params;
	}

	@Override
	public void execute(List<API> apis) throws AppException {
		cal.add(Calendar.DAY_OF_YEAR, checkCertParams.getNumberOfDays());
		System.out.println("Going to check certificates expiration of: " + apis.size() + " selected API(s) within the next "+checkCertParams.getNumberOfDays()+" days (Not valid after: "+formatDate(cal.getTime().getTime())+").");
		List<ApiPlusCert> expiredCerts = new ArrayList<ApiPlusCert>();
		for(API api : apis) {
			if(api.getCaCerts()==null) continue;
			List<CaCert> certificates = api.getCaCerts();
			for(CaCert certificate : certificates) {
				try {
					Date notValidAfter = new Date(certificate.getNotValidAfter());
					if(notValidAfter.before(cal.getTime())) {
						expiredCerts.add(new ApiPlusCert(api, certificate));
					}
				} catch (Exception e) {
					LOG.error("Error checking certificate: "+certificate.getAlias()+" expiration date used by API: "+api.toStringHuman()+".", e);
					this.result.setError(ErrorCode.CHECK_CERTS_UNXPECTED_ERROR);
				}
			}
		}
		if(expiredCerts.size()>0) {
			this.result.setError(ErrorCode.CHECK_CERTS_FOUND_CERTS);
			this.result.setResultDetails(expiredCerts);
			System.out.println("The following certificates will expire in the next "+checkCertParams.getNumberOfDays()+" days.");
			System.out.println(AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS, expiredCerts, Arrays.asList(
					new Column().header("API-Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getId()),
					new Column().header("API-Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getName()),
					new Column().header("API-Path").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.api.getPath()),
					new Column().header("API-Ver.").with(expired -> expired.api.getVersion()),
					new Column().header("Certificate-Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.certificate.getName()),
					new Column().header("Not valid after").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> formatDate(expired.certificate.getNotValidAfter())),
					new Column().header("Not valid before").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> formatDate(expired.certificate.getNotValidBefore())),
					new Column().header("MD5-Fingerprint").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(expired -> expired.certificate.getMd5Fingerprint())
					)));
		} else {
			System.out.println("No certificates found that will expire within the next "+checkCertParams.getNumberOfDays()+" days.");
		}
		System.out.println("Done!");
		return;
	}

	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();
		return builder.build();
	}
	
	private class ApiPlusCert {
		CaCert certificate;
		API api;
		public ApiPlusCert(API api, CaCert certificate) {
			super();
			this.certificate = certificate;
			this.api = api;
		}
	}
	
	private String formatDate(Long date) {
		return dateFormatter.format(new Date(date));
	}
}
