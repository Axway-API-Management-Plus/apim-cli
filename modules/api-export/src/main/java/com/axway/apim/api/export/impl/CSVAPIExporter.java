package com.axway.apim.api.export.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIComparator;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.export.lib.ClientAppComparator;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class CSVAPIExporter extends APIResultHandler {
	private static Logger LOG = LoggerFactory.getLogger(CSVAPIExporter.class);
	
	private static enum HeaderFields {
		standard(new String[] {
				"API ID", 
				"API Name", 
				"API Path", 
				"API Version"
				}),
		wide(new String[] {
				"API ID", 
				"API Organization", 
				"API Name", 
				"API Path", 
				"API Version", 
				"API V-Host", 
				"API State", 
				"Backend", 
				"Request Policy", 
				"Routing Policy", 
				"Response Policy", 
				"Fault-Handler Policy"
				}),
		ultra(new String[] {
				"API ID",
				"API Organization", 
				"API Name",
				"API Path",
				"API Version",
				"API V-Host",
				"API State", 
				"Backend", 
				"Security",
				"Request Policy",
				"Routing Policy",
				"Response Policy",
				"Fault-Handler Policy",
				"Custom-Properties",
				"Tags",
				"Granted Organization",
				"Application Name",
				"Application Organization"
				});
		
		String[] headerFields;

		private HeaderFields(String[] headerFields) {
			this.headerFields = headerFields;
		}
	}

	APIManagerAdapter apiManager;
	
	public CSVAPIExporter(APIExportParams params) throws AppException {
		super(params);
	}
	
	@Override
	public void execute(List<API> apis) throws AppException {
		CSVPrinter csvPrinter = null;
		Wide wide = params.getWide();
		String givenTarget = params.getTarget();
		try {
			File target = new File(givenTarget);
			if(target.isDirectory()) {
				target = new File(givenTarget + File.separator + createFileName());
			}
			if(target.exists() && !params.deleteTarget()) {
				ErrorState.getInstance().setError("Targetfile: " + target.getCanonicalPath() + " already exists. You may set the flag -deleteTarget if you wish to overwrite it.", ErrorCode.EXPORT_FOLDER_EXISTS, false);
				throw new AppException("Targetfile: " + target.getCanonicalPath() + " already exists.", ErrorCode.EXPORT_FOLDER_EXISTS);
			}
			Appendable appendable = new FileWriter(target);
			appendable.append("sep=,\n"); // Helps Excel to detect columns
			csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(HeaderFields.valueOf(wide.name()).headerFields));
			writeRecords(csvPrinter, apis, wide);
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
		if(params.getValue("stage")!=null) {
			host = params.getValue("stage");
		}
		return "api_export_"+host+"_"+APIManagerAdapter.getCurrentUser(false).getLoginName()+"_"+dateTime+".csv";
	}
	
	private void writeRecords(CSVPrinter csvPrinter, List<API> apis, Wide wide) throws IOException, AppException {
		apis.sort(new APIComparator());
		int i=0;
		for(API api : apis) {
			if( i % 50 == 0 ){
				csvPrinter.flush();
			}
			// Don't show Orgs and App if view is not ULTRA
			if(wide.equals(Wide.ultra)) {
				if(api.getApplications()!=null) api.getApplications().sort(new ClientAppComparator());
				for(Organization org : getClientOrganizations(api)) {
					for(ClientApplication app : getClientApplications(api)) {
						writeRecords(csvPrinter, api, app, org, wide);
						if( i % 50 == 0 ){
							csvPrinter.flush();
						}
					}
					if( i % 50 == 0 ){
						csvPrinter.flush();
					}
				}
			} else {
				writeRecords(csvPrinter, api, null, null, wide);
			}
		}
		csvPrinter.flush();
	}
	
	private List<Organization> getClientOrganizations(API api) throws AppException {
		if(api.getClientOrganizations()==null || api.getClientOrganizations().size()==0) {
			Organization org = new Organization();
			org.setName("N/A");
			List<Organization> ungranted = new ArrayList<Organization>();
			ungranted.add(org);
			return ungranted;
		} else {
			return api.getClientOrganizations();
		}
	}
	
	private List<ClientApplication> getClientApplications(API api) throws AppException {
		if(api.getApplications()==null || api.getApplications().size()==0) {
			ClientApplication app = new ClientApplication();
			app.setName("N/A");
			Organization org = new Organization();
			org.setName("N/A");
			app.setOrganization(org);
			List<ClientApplication> subscribed = new ArrayList<ClientApplication>();
			subscribed.add(app);
			return subscribed;
		} else {
			return api.getApplications();
		}
	}
	
	private void writeRecords(CSVPrinter csvPrinter, API api, ClientApplication app, Organization org, Wide wide) throws IOException, AppException {
		switch(wide) {
		case standard:
			writeStandardToCSV(csvPrinter, api);
			break;
		case wide:
			writeWideToCSV(csvPrinter, api);
			break;
		case ultra:
			writeAPIUltraToCSV(csvPrinter, api, app, org);
			break;
		default:
			break;
		}
	}
	
	private void writeStandardToCSV(CSVPrinter csvPrinter, API api) throws IOException, AppException {
		csvPrinter.printRecord(
				api.getId(), 
				api.getName(), 
				api.getPath(), 
				api.getVersion()
		);
	}
	
	private void writeWideToCSV(CSVPrinter csvPrinter, API api) throws IOException, AppException {
		csvPrinter.printRecord(
				api.getId(), 
				api.getOrganization().getName(),
				api.getName(), 
				api.getPath(), 
				api.getVersion(), 
				api.getVhost(), 
				api.getState(), 
				getBackendPath(api), 
				getUsedPolicies(api, PolicyType.REQUEST),
				getUsedPolicies(api, PolicyType.ROUTING),
				getUsedPolicies(api, PolicyType.RESPONSE),
				getUsedPolicies(api, PolicyType.FAULT_HANDLER)
		);
	}
	
	private void writeAPIUltraToCSV(CSVPrinter csvPrinter, API api, ClientApplication app, Organization org) throws IOException, AppException {
		csvPrinter.printRecord(
				api.getId(), 
				api.getOrganization().getName(),
				api.getName(), 
				api.getPath(), 
				api.getVersion(), 
				api.getVhost(), 
				api.getState(), 
				getBackendPath(api), 
				getUsedSecurity(api), 
				getUsedPolicies(api, PolicyType.REQUEST),
				getUsedPolicies(api, PolicyType.ROUTING),
				getUsedPolicies(api, PolicyType.RESPONSE),
				getUsedPolicies(api, PolicyType.FAULT_HANDLER), 
				getCustomProps(api),
				getTags(api) , 
				org.getName(), 
				app.getName(), 
				app.getOrganization().getName()
		);
	}
	
	@Override
	public APIFilter getFilter() {
		Builder builder = getBaseAPIFilterBuilder();

		switch(params.getWide()) {
		case standard:
		case wide:
			builder.includeQuotas(false);
			builder.includeClientApplications(false);
			builder.includeClientOrganizations(false);
			builder.includeClientAppQuota(false);
			builder.includeQuotas(false);
			break;
		case ultra:
			builder.includeQuotas(true);
			builder.includeClientAppQuota(false);
			builder.includeClientApplications(true);
			builder.includeClientOrganizations(true);
			break;
		}		
		APIFilter filter = builder.build();
		return filter;
	}
}
