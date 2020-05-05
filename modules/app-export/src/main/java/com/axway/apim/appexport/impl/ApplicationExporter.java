package com.axway.apim.appexport.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class ApplicationExporter {
	
	protected static Logger LOG = LoggerFactory.getLogger(ApplicationExporter.class);
	
	List<ClientApplication> apps;
	
	String targetFolder;
	
	boolean hasError = false;

	public ApplicationExporter(List<ClientApplication> apps, String folderToExport) {
		this.apps = apps;
		this.targetFolder = (folderToExport==null) ? "." : folderToExport;
	}
	
	public abstract void export() throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
}
