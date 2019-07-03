package com.axway.apim.metadata.export.formats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.metadata.export.beans.APIManagerExportMetadata;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.apiAccess.APIAccess;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.state.APIMethod;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;

public class CSVMetadataExport implements IMetadataExport {
	
	private static Logger LOG = LoggerFactory.getLogger(CSVMetadataExport.class);
	
	APIManagerAdapter mgrAdapater;
	APIManagerExportMetadata metaData;
	
	private Map<String, IAPI> APIsPerId;
	
	public CSVMetadataExport() {
		super();
	}
	public APIManagerExportMetadata getMetaData() {
		return metaData;
	}
	public void setMetaData(APIManagerExportMetadata metaData) {
		this.metaData = metaData;
	}
	public void setMgrAdapater(APIManagerAdapter mgrAdapater) {
		this.mgrAdapater = mgrAdapater;
	}
	@Override
	public void preProcessMetadata() throws AppException {
		// We need to know the API-Access for each Application
		initApplicationAPISubcription();
		LOG.info("Successfully loaded application subscriptions.");
		// And the API-Method for each APIs
		initAPIMethods();
		LOG.info("Successfully loaded all API-Methods.");
	}
	@Override
	public void exportMetadata() throws AppException {
		String filename = CommandParameters.getInstance().getValue("filename");
		Appendable appendable;
		CSVPrinter csvPrinter = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		try {
			File cvsFile = new File(filename);
			if(cvsFile.exists()) {
				LOG.info("Going to overwrite existing file: '"+cvsFile.getCanonicalPath()+"'");
			}
			appendable = new FileWriter(cvsFile);
			csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader("ClientApplication", "API", "APIMethod", "GatewayInstanceType", "Organization", "EventTimeStamp"));
			int i=0;
			for(ClientApplication app : this.metaData.getAllApps()) {
				LOG.debug("Handling application: '"+app.getName()+"'");
				for(APIAccess apiAccess : app.getApiAccess()) {
					IAPI api = APIsPerId.get(apiAccess.getApiId());
					if(api==null) {
						LOG.warn("Application: '"+app.getName()+"' has a subscription to non-existing API with ID: '"+apiAccess.getId()+"'");
						continue;
					}
					LOG.trace("Export details of subscribed API: '"+api.getName()+"'");
					for(APIMethod method : ((ActualAPI)api).getApiMethods()) {
						i++;
						csvPrinter.printRecord(app.getId(), api.getId(), method.getId(), "Front-End", app.getOrganizationId(), df.format(getMidnight()));
						if( i % 50 == 0 ){
							csvPrinter.flush();
						}
					}
				}
			}
			LOG.info("API-Metadata information exported into file: '"+cvsFile.getCanonicalPath()+"'");
		} catch (IOException e) {
			throw new AppException("Cant open CSV-File for writing", ErrorCode.UNXPECTED_ERROR);
		} finally {
			if(csvPrinter!=null)
				try {
					csvPrinter.close(true);
				} catch (Exception ignore) {
					throw new AppException("Unable to close CSVWriter", ErrorCode.UNXPECTED_ERROR, ignore);
				}
		}
		
	}
	
	private void initApplicationAPISubcription() throws AppException {
		for(ClientApplication app : metaData.getAllApps()) {
			app.setApiAccess(APIManagerAdapter.getAPIAccess(app.getId(), "applications"));
		}
	}
	private void initAPIMethods() throws AppException {
		APIsPerId = new HashMap<String, IAPI>();
		for(IAPI api : metaData.getAllAPIs()) {
			((ActualAPI)api).setApiMethods(mgrAdapater.getAllMethodsForAPI(api.getId()));
			APIsPerId.put(api.getId(), api);
		}
	}
	
	private Date getMidnight() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}
	
}
