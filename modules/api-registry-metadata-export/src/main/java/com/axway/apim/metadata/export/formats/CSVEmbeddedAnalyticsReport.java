package com.axway.apim.metadata.export.formats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.apiAccess.APIAccess;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.state.APIMethod;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;

public class CSVEmbeddedAnalyticsReport extends AbstractReportFormat implements IReportFormat {
	
	private static Logger LOG = LoggerFactory.getLogger(CSVEmbeddedAnalyticsReport.class);
	
	private Map<String, IAPI> APIsPerId;
	
	public CSVEmbeddedAnalyticsReport() {
		super();
	}

	@Override
	public void preProcessMetadata() throws AppException {
		// We need to know the API-Access for each Application
		initApplicationAPISubcription();
		LOG.info("Successfully loaded application subscriptions.");
		// And the API-Method for each APIs
		this.APIsPerId = getAPIMethods();
		LOG.info("Successfully loaded all API-Methods.");
	}
	@Override
	public void exportMetadata() throws AppException {
		String filename = CommandParameters.getInstance().getValue("filename");
		Appendable appendable;
		CSVPrinter csvPrinter = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			File cvsFile = new File(filename);
			if(cvsFile.exists()) {
				LOG.info("Going to overwrite existing file: '"+cvsFile.getCanonicalPath()+"'");
			}
			appendable = new FileWriter(cvsFile);
			csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(
				"ClientApplicationId", 
				"ClientApplication", 
				"APIId", 
				"API", 
				"APIMethodId", 
				"APIMethod", 
				"GatewayInstanceType", 
				"OrganizationId", 
				"Organization", 
				"EventTimeStamp"
			));
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
						csvPrinter.printRecord(
								app.getId(), 
								app.getName(), 
								api.getId(), 
								api.getName(), 
								method.getId(), 
								method.getName(), 
								"Front-End", 
								app.getOrganizationId(), 
								APIManagerAdapter.getInstance().getOrgName(app.getOrganizationId()), 
								df.format(getMidnight()
						));
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
	
	private Date getMidnight() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}
	
}
