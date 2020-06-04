package com.axway.apim.appexport.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class ApplicationExporter {
	
	protected static Logger LOG = LoggerFactory.getLogger(ApplicationExporter.class);
	
	List<ClientApplication> apps;
	
	String targetFolder;
	
	boolean hasError = false;
	
	public static ApplicationExporter create(List<ClientApplication> apps, String folderToExport) {
		return new JsonApplicationExporter(apps, folderToExport);
	}

	public ApplicationExporter(List<ClientApplication> apps, String folderToExport) {
		this.apps = apps;
		this.targetFolder = (folderToExport==null) ? "." : folderToExport;
	}
	
	public abstract void export() throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {

		try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
			fileOuputStream.write(bFile);
		} catch (IOException e) {
			throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public static void storeCaCert(File localFolder, String certBlob, String filename) throws AppException {
		try {
			writeBytesToFile(certBlob.getBytes(), localFolder + "/" + filename);
		} catch (AppException e) {
			throw new AppException("Can't write certificate to disc", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	protected void removeApplicationDefaultQuota(ClientApplication app) {
		if(app.getAppQuota()==null) return;
		if(app.getAppQuota().getId().equals(APIManagerAdapter.APPLICATION_DEFAULT_QUOTA)) {
			app.setAppQuota(null);
		}
	}
}
