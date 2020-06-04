package com.axway.apim.appexport.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class ApplicationExporter {
	
	protected static Logger LOG = LoggerFactory.getLogger(ApplicationExporter.class);
	
	public enum ExportImpl {
		JSON_EXPORTER(JsonApplicationExporter.class),
		CONSOLE_EXPORTER(ConsoleAppExporter.class);
		
		private final Class<ApplicationExporter> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ExportImpl(Class clazz) {
			this.implClass = clazz;
		}

		public Class<ApplicationExporter> getClazz() {
			return implClass;
		}
	}
	
	List<ClientApplication> apps;
	
	AppExportParams params;
	
	boolean hasError = false;
	
	public static ApplicationExporter create(List<ClientApplication> apps, ExportImpl exportImpl, AppExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { apps, params };
			Constructor<ApplicationExporter> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{List.class, AppExportParams.class});
			ApplicationExporter exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public ApplicationExporter(List<ClientApplication> apps, AppExportParams params) {
		this.apps = apps;
		this.params = params;
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
