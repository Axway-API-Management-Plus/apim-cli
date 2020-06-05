package com.axway.apim.api.export.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class APIExporter {

	protected static Logger LOG = LoggerFactory.getLogger(APIExporter.class);
	
	List<API> apis;
	
	APIExportParams params;
	
	boolean hasError = false;
	
	public enum ExportImpl {
		JSON_EXPORTER(JsonAPIExporter.class),
		CONSOLE_EXPORTER(ConsoleAPIExporter.class);
		
		private final Class<APIExporter> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ExportImpl(Class clazz) {
			this.implClass = clazz;
		}

		public Class<APIExporter> getClazz() {
			return implClass;
		}
	}

	public APIExporter(List<API> apis, APIExportParams params) {
		this.apis = apis;
		this.params = params;
	}
	
	public static APIExporter create(List<API> apps, ExportImpl exportImpl, APIExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { apps, params };
			Constructor<APIExporter> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{List.class, APIExportParams.class});
			APIExporter exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public abstract void export() throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}

}
