package com.axway.apim.appexport.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.clientApps.ClientAppFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.APIAccessComparator;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appexport.lib.ApplicationComparator;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CSVAppExporter extends ApplicationExporter {
	private static final Logger LOG = LoggerFactory.getLogger(CSVAppExporter.class);
	
	private enum HeaderFields {
		STANDARD(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled",
				"Created by"
				}),
		WIDE(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled",
				"Created by",
				"API Quota",
				"API-Method",
				"Quota Config"
				}),
		ULTRA(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled",
				"Created by",
				"API-Name", 
				"API-Version", 
				"Access created by", 
				"Access created on"
				});
		
		final String[] headerFields;

		HeaderFields(String[] headerFields) {
			this.headerFields = headerFields;
		}
	}

	APIManagerAdapter apiManager;
	
	public CSVAppExporter(AppExportParams params, ExportResult result) throws AppException {
		super(params, result);
		apiManager = APIManagerAdapter.getInstance();
	}
	
	@Override
	public void export(List<ClientApplication> apps) throws AppException {
		Wide wide = params.getWide();
		String givenTarget = params.getTarget();
		try {
			File target = new File(givenTarget);
			if(target.isDirectory()) {
				target = new File(givenTarget + File.separator + createFileName());
			}
			if(target.exists() && !params.isDeleteTarget()) {
				throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS);
			}
			try(FileWriter appendable = new FileWriter(target)) {
				appendable.append("sep=,\n"); // Helps Excel to detect columns
				try (CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(HeaderFields.valueOf(wide.name()).headerFields))) {
					writeRecords(csvPrinter, apps, wide);
					LOG.info("API export successfully written to file: {}", target.getCanonicalPath());
				}
			}
		} catch (IOException e1) {
			throw new AppException("Cant open CSV-File: "+givenTarget+" for writing", ErrorCode.UNXPECTED_ERROR, e1);
		}
	}
	
	private String createFileName() throws AppException {
		DateFormat df = new SimpleDateFormat("ddMMyyyy-HHmm");
		String dateTime = df.format(new Date());
		String host = params.getAPIManagerURL().getHost();
		if(params.getStage()!=null) {
			host = params.getStage();
		}
		return "app_export_"+host+"_"+APIManagerAdapter.getCurrentUser(false).getLoginName()+"_"+dateTime+".csv";
	}
	
	private void writeRecords(CSVPrinter csvPrinter, List<ClientApplication> apps, Wide wide) throws IOException {
		apps.sort(new ApplicationComparator());
		int i=0;
		for(ClientApplication app : apps) {
			if( i % 50 == 0 ){
				csvPrinter.flush();
			}
			// With wide - Report the application quotas
			if(wide.equals(Wide.wide)) {
				if(app.getAppQuota()!=null && app.getAppQuota().getRestrictions()!=null && app.getAppQuota().getRestrictions().size()!=0) {
					for(QuotaRestriction restriction : app.getAppQuota().getRestrictions()) {
						writeRecords(csvPrinter, app, null, restriction, wide);	
					}
				} else {
					writeRecords(csvPrinter, app, null, null, wide);
				}
			
			// With ultra - Report all subscribed APIs
			} else if(wide.equals(Wide.ultra)) {
				if(app.getApiAccess()!=null);
				app.getApiAccess().sort(new APIAccessComparator());
				for(APIAccess apiAccess : app.getApiAccess()) {
					writeRecords(csvPrinter, app, apiAccess, null, wide);	
				}
			} else {
				writeRecords(csvPrinter, app, null, null, wide);
			}
			i++;
		}
		csvPrinter.flush();
	}
	
	private void writeRecords(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess, QuotaRestriction restriction, Wide wide) throws IOException {
		switch(wide) {
		case standard:
			writeStandardToCSV(csvPrinter, app);
			break;
		case wide:
			writeWideToCSV(csvPrinter, app, restriction);
			break;
		case ultra:
			writeUltraToCSV(csvPrinter, app, apiAccess);
			break;
		default:
			break;
		}
	}
	
	private void writeStandardToCSV(CSVPrinter csvPrinter, ClientApplication app) throws IOException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled(),
				getCreatedBy(app.getCreatedBy(), app)
		);
	}
	
	private void writeWideToCSV(CSVPrinter csvPrinter, ClientApplication app, QuotaRestriction quotaRestriction) throws IOException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled(),
				getCreatedBy(app.getCreatedBy(), app),
				getRestrictedAPI(quotaRestriction),
				getRestrictedMethod(quotaRestriction),
				getQuotaConfig(quotaRestriction)
		);
	}
	
	private void writeUltraToCSV(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess) throws IOException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled(),
				getCreatedBy(app.getCreatedBy(), app),
				apiAccess.getApiName(),
				apiAccess.getApiVersion(), 
				getCreatedBy(apiAccess.getCreatedBy(), app),
				getCreatedOn(apiAccess.getCreatedOn())
		);
	}
	
	private String getRestrictedAPI(QuotaRestriction quotaRestriction) throws AppException {
		if(quotaRestriction==null) return "N/A";
		API api = apiManager.apiAdapter.getAPIWithId(quotaRestriction.getApiId());
		if(api==null) return "Err";
		return api.getName();
	}
	
	private String getRestrictedMethod(QuotaRestriction quotaRestriction) throws AppException {
		if(quotaRestriction==null) return "N/A";
		API restrictedAPI = apiManager.apiAdapter.getAPIWithId(quotaRestriction.getApiId());
		if(restrictedAPI==null) return "Err";
		return quotaRestriction.getMethod().equals("*") ? "All Methods" : apiManager.methodAdapter.getMethodForId(restrictedAPI.getId(), quotaRestriction.getMethod()).getName();
	}
	
	private String getQuotaConfig(QuotaRestriction quotaRestriction) {
		if(quotaRestriction==null) return "N/A";
		return ""+quotaRestriction.getConfig();
	}
	
	@Override
	public ClientAppFilter getFilter() throws AppException {
		Builder builder = getBaseFilterBuilder();

		switch(params.getWide()) {
		case standard:
			break;
		case wide:
			builder.includeQuotas(true);
			break;
		case ultra:
			builder.includeAPIAccess(true);
			break;
		}
		return builder.build();
	}
}
