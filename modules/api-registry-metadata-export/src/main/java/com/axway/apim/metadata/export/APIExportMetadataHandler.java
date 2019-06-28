package com.axway.apim.metadata.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.metadata.export.beans.APIManagerExportMetadata;
import com.axway.apim.metadata.export.formats.CSVMetadataExport;
import com.axway.apim.metadata.export.formats.IMetadataExport;
import com.axway.apim.swagger.APIManagerAdapter;

public class APIExportMetadataHandler {
	
	private static Logger LOG = LoggerFactory.getLogger(APIExportMetadataHandler.class);
	
	APIManagerAdapter mgrAdapater;
	
	String exportFormatClassname;
	
	APIManagerExportMetadata exportData = new APIManagerExportMetadata();

	public APIExportMetadataHandler(APIManagerAdapter apimAdapter, String exportFormatClassname) throws AppException {
		super();
		this.mgrAdapater = apimAdapter;
		// Initialize the base-data from API-Manager!
		exportData.setAllOrgs(mgrAdapater.getAllOrgs());
		LOG.info("Susccessfully retrieved " + exportData.getAllOrgs().size() + " organizations from API-Manager");
		exportData.setAllApps(mgrAdapater.getAllApps());
		LOG.info("Susccessfully retrieved " + exportData.getAllApps().size() + " applications from API-Manager");
		exportData.setAllAPIs(mgrAdapater.getAllAPIs());
		LOG.info("Susccessfully retrieved " + exportData.getAllAPIs().size() + " APIs from API-Manager");
		// All the rest is up to the specific export format
	}
	
	public void exportMetadata() throws AppException {
		// Detect the format (right now just static
		// Later use we may use the exportFormatClassname to use the desired format
		IMetadataExport metadata = new CSVMetadataExport();
		metadata.setMgrAdapater(mgrAdapater);
		metadata.setMetaData(exportData);
		metadata.preProcessMetadata();
		metadata.exportMetadata();
	}
}
