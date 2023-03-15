package com.axway.apim.setup.impl;

import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.adapter.apis.RemoteHostFilter.Builder;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;

import java.lang.reflect.Constructor;

public abstract class APIManagerSetupResultHandler {
	public enum ResultHandler {
		JSON_EXPORTER(JsonAPIManagerSetupExporter.class),
		YAML_EXPORTER(YamlAPIManagerSetupExporter.class),
		CONSOLE_EXPORTER(ConsoleAPIManagerSetupExporter.class);
		
		private final Class<APIManagerSetupResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ResultHandler(Class clazz) {
			this.implClass = clazz;
		}

		public Class<APIManagerSetupResultHandler> getClazz() {
			return implClass;
		}
	}
	
	APIManagerSetupExportParams params;
	ExportResult result;
	
	boolean hasError = false;
	
	public static APIManagerSetupResultHandler create(ResultHandler exportImpl, APIManagerSetupExportParams params, Result result) throws AppException {
		try {
			Object[] intArgs = new Object[] { params, result };
			Constructor<APIManagerSetupResultHandler> constructor =
					exportImpl.getClazz().getConstructor(APIManagerSetupExportParams.class, ExportResult.class);
			return constructor.newInstance(intArgs);
		} catch (Exception e) {
			throw new AppException("Error initializing config exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	protected APIManagerSetupResultHandler(APIManagerSetupExportParams params, ExportResult result) {
		this.params = params;
		this.result = result;
	}
	
	public abstract void export(APIManagerConfig config) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getRemoteHostBaseFilterBuilder() {
		return new Builder()
				.hasName(params.getRemoteHostName())
				.hasId(params.getRemoteHostId());
	}
	
	public abstract RemoteHostFilter getRemoteHostFilter() throws AppException;
}
