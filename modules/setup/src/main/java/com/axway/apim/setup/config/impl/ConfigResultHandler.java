package com.axway.apim.setup.config.impl;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.OrgFilter.Builder;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.setup.config.ConfigExportParams;

public abstract class ConfigResultHandler {
	
	protected static Logger LOG = LoggerFactory.getLogger(ConfigResultHandler.class);
	
	public enum ResultHandler {
		JSON_EXPORTER(JsonConfigExporter.class),
		CONSOLE_EXPORTER(ConsoleConfigExporter.class);
		
		private final Class<ConfigResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ResultHandler(Class clazz) {
			this.implClass = clazz;
		}

		public Class<ConfigResultHandler> getClazz() {
			return implClass;
		}
	}
	
	ConfigExportParams params;
	
	boolean hasError = false;
	
	public static ConfigResultHandler create(ResultHandler exportImpl, ConfigExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<ConfigResultHandler> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{ConfigExportParams.class});
			ConfigResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public ConfigResultHandler(ConfigExportParams params) {
		this.params = params;
	}
	
	public abstract void export(APIManagerConfig config) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getBaseConfigFilterBuilder() {
		Builder builder = new OrgFilter.Builder()
				.hasId(params.getFieldName());
		return builder;
	}
	
	public abstract OrgFilter getFilter() throws AppException;
}
