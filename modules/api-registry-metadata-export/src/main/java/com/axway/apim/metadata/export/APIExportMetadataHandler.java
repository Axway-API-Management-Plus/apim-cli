package com.axway.apim.metadata.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.metadata.export.beans.APIManagerExportMetadata;
import com.axway.apim.metadata.export.formats.IReportFormat;
import com.axway.apim.swagger.APIManagerAdapter;

public class APIExportMetadataHandler {
	
	private static Logger LOG = LoggerFactory.getLogger(APIExportMetadataHandler.class);
	
	APIManagerAdapter mgrAdapater;
	
	Class<IReportFormat> reportClass;
	
	APIManagerExportMetadata exportData = new APIManagerExportMetadata();

	public APIExportMetadataHandler(APIManagerAdapter apimAdapter, String exportFormatClassname) throws AppException {
		super();
		try {
			if(exportFormatClassname.indexOf(".")==-1) {
				exportFormatClassname = "com.axway.apim.metadata.export.formats."+exportFormatClassname;
			}
			this.reportClass = (Class<IReportFormat>) Class.forName(exportFormatClassname);
		} catch (ClassNotFoundException e) {
			throw new AppException("Unable to find report class. ", ErrorCode.UNXPECTED_ERROR, e);
		}
		this.mgrAdapater = apimAdapter;
		// Initialize the base-data from API-Manager!
		exportData.setAllOrgs(mgrAdapater.getAllOrgs());
		LOG.info("Successfully retrieved " + exportData.getAllOrgs().size() + " organizations from API-Manager");
		exportData.setAllApps(mgrAdapater.getAllApps());
		LOG.info("Successfully retrieved " + exportData.getAllApps().size() + " applications from API-Manager");
		exportData.setAllAPIs(mgrAdapater.getAllAPIs());
		LOG.info("Successfully retrieved " + exportData.getAllAPIs().size() + " APIs from API-Manager");
		// All the rest is up to the specific export format
	}
	
	public void exportMetadata() throws AppException {		
		IReportFormat metadata;
		try {
			metadata = reportClass.newInstance();
			metadata.setMgrAdapater(mgrAdapater);
			metadata.setMetaData(exportData);
			metadata.preProcessMetadata();
			metadata.exportMetadata();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new AppException("Cant initialize report class", ErrorCode.UNXPECTED_ERROR, e);
		}

	}
}
