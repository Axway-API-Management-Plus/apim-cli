package com.axway.apim.metadata.export.formats;

import com.axway.apim.lib.AppException;
import com.axway.apim.metadata.export.beans.APIManagerExportMetadata;
import com.axway.apim.swagger.APIManagerAdapter;

public interface IMetadataExport {
	public void setMetaData(APIManagerExportMetadata metaData);
	
	public void setMgrAdapater(APIManagerAdapter mgrAdapater);
	
	public void preProcessMetadata() throws AppException;
	
	public void exportMetadata() throws AppException;

}
