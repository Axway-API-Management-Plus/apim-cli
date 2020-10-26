package com.axway.apim.appexport.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.axway.apim.lib.errorHandling.ErrorState;

public class CSVAppExporter extends ApplicationExporter {
	private static Logger LOG = LoggerFactory.getLogger(CSVAppExporter.class);
	
	private static enum HeaderFields {
		standard(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled"
				}),
		wide(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled",
				"API Quota",
				"API-Method",
				"Quota Config"
				}),
		ultra(new String[] {
				"ID", 
				"Organization", 
				"Name", 
				"Email", 
				"Phone", 
				"State", 
				"Enabled",
				"API-Name", 
				"API-Version", 
				"Access created by", 
				"Access created on"
				});
		
		String[] headerFields;

		private HeaderFields(String[] headerFields) {
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
		CSVPrinter csvPrinter = null;
		Wide wide = params.getWide();
		String givenTarget = params.getTarget();
		try {
			File target = new File(givenTarget);
			if(target.isDirectory()) {
				target = new File(givenTarget + File.separator + createFileName());
			}
			if(target.exists() && !params.isDeleteTarget()) {
				ErrorState.getInstance().setError("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS, false);
				throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists.", ErrorCode.EXPORT_FOLDER_EXISTS);
			}
			Appendable appendable = new FileWriter(target);
			appendable.append("sep=,\n"); // Helps Excel to detect columns
			csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(HeaderFields.valueOf(wide.name()).headerFields));
			writeRecords(csvPrinter, apps, wide);
			LOG.info("API export successfully written to file: " + target.getCanonicalPath());
		} catch (IOException e1) {
			throw new AppException("Cant open CSV-File: "+givenTarget+" for writing", ErrorCode.UNXPECTED_ERROR, e1);
		} finally {
			if(csvPrinter!=null)
				try {
					csvPrinter.close(true);
				} catch (Exception ignore) {
					throw new AppException("Unable to close CSVWriter", ErrorCode.UNXPECTED_ERROR, ignore);
				}
		}
	}
	
	private String createFileName() throws AppException {
		DateFormat df = new SimpleDateFormat("ddMMyyyy-HHmm");
		String dateTime = df.format(new Date());
		String host = params.getHostname();
		if(params.getStage()!=null) {
			host = params.getStage();
		}
		return "app_export_"+host+"_"+APIManagerAdapter.getCurrentUser(false).getLoginName()+"_"+dateTime+".csv";
	}
	
	private void writeRecords(CSVPrinter csvPrinter, List<ClientApplication> apps, Wide wide) throws IOException, AppException {
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
	
	private void writeRecords(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess, QuotaRestriction restriction, Wide wide) throws IOException, AppException {
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
	
	private void writeStandardToCSV(CSVPrinter csvPrinter, ClientApplication app) throws IOException, AppException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled()
		);
	}
	
	private void writeWideToCSV(CSVPrinter csvPrinter, ClientApplication app, QuotaRestriction quotaRestriction) throws IOException, AppException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled(),
				getRestrictedAPI(quotaRestriction),
				getRestrictedMethod(quotaRestriction),
				getQuotaConfig(quotaRestriction)
		);
	}
	
	private void writeUltraToCSV(CSVPrinter csvPrinter, ClientApplication app, APIAccess apiAccess) throws IOException, AppException {
		csvPrinter.printRecord(
				app.getId(),
				app.getOrganization().getName(),
				app.getName(), 
				app.getEmail(),
				app.getPhone(),
				app.getState(),
				app.isEnabled(),
				apiAccess.getApiName(),
				apiAccess.getApiVersion(), 
				getCreatedBy(apiAccess.getCreatedBy()),
				getCreatedOn(apiAccess.getCreatedOn())
		);
	}
	
	private String getRestrictedAPI(QuotaRestriction quotaRestriction) throws AppException {
		if(quotaRestriction==null) return "N/A";
		API api = apiManager.apiAdapter.getAPIWithId(quotaRestriction.getApi());
		if(api==null) return "Err";
		return api.getName();
	}
	
	private String getRestrictedMethod(QuotaRestriction quotaRestriction) throws AppException {
		if(quotaRestriction==null) return "N/A";
		API restrictedAPI = apiManager.apiAdapter.getAPIWithId(quotaRestriction.getApi());
		if(restrictedAPI==null) return "Err";
		return quotaRestriction.getMethod().equals("*") ? "All Methods" : apiManager.methodAdapter.getMethodForId(restrictedAPI.getId(), quotaRestriction.getMethod()).getName();
	}
	
	private String getQuotaConfig(QuotaRestriction quotaRestriction) throws AppException {
		if(quotaRestriction==null) return "N/A";
		return ""+quotaRestriction.getConfig();
	}
	
	private String getCreatedBy(String userId) {
		try {
			return apiManager.userAdapter.getUserForId(userId).getLoginName();
		} catch (AppException e) {
			LOG.error("Error getting createdBy user with Id: " + userId,e);
			return "Err";
		}
	}
	
	private Date getCreatedOn(Long createdOn) {
		return new Date(createdOn);
	}
	
	@Override
	public ClientAppFilter getFilter() throws AppException {
		Builder builder = getBaseFilterBuilder();

		switch(params.getWide()) {
		case standard:
		case wide:
			builder.includeQuotas(true);
			break;
		case ultra:
			builder.includeAPIAccess(true);
			break;
		}		
		ClientAppFilter filter = builder.build();
		return filter;
	}
}
