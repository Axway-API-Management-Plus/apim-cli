package com.axway.apim.setup.remotehosts.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.adapter.apis.RemoteHostFilter.Builder;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;

public abstract class RemoteHostsResultHandler {
	
	protected static Logger LOG = LoggerFactory.getLogger(RemoteHostsResultHandler.class);
	
	RemoteHostsExportParams params;
	ExportResult result;
	
	boolean hasError = false;
	
	public enum ResultHandler {
		JSON_EXPORTER(JsonRemoteHostsExporter.class),
		CONSOLE_EXPORTER(ConsoleRemoteHostsExporter.class);
		
		private final Class<RemoteHostsResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ResultHandler(Class clazz) {
			this.implClass = clazz;
		}

		public Class<RemoteHostsResultHandler> getClazz() {
			return implClass;
		}
	}
	
	public static RemoteHostsResultHandler create(ResultHandler exportImpl, RemoteHostsExportParams params, ExportResult result) throws AppException {
		try {
			Object[] intArgs = new Object[] { params, result };
			Constructor<RemoteHostsResultHandler> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{RemoteHostsExportParams.class, ExportResult.class});
			RemoteHostsResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public RemoteHostsResultHandler(RemoteHostsExportParams params, ExportResult result) {
		this.params = params;
		this.result = result;
	}
	
	public abstract void export(List<RemoteHost> remoteHosts) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getBaseFilterBuilder() {
		Builder builder = new RemoteHostFilter.Builder()
				.hasName(params.getName())
				.hasId(params.getId());
		return builder;
	}
	
	public abstract RemoteHostFilter getFilter() throws AppException;
}
