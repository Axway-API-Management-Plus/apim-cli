package com.axway.apim.appexport.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.clientApps.ClientAppFilter.Builder;
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
	
	AppExportParams params;
	
	boolean hasError = false;
	
	public static ApplicationExporter create(ExportImpl exportImpl, AppExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<ApplicationExporter> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{AppExportParams.class});
			ApplicationExporter exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public ApplicationExporter(AppExportParams params) {
		this.params = params;
	}
	
	public abstract void export(List<ClientApplication> apps) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public abstract ClientAppFilter getFilter() throws AppException;
	
	protected Builder getBaseFilterBuilder() throws AppException {
		Builder builder = new ClientAppFilter.Builder()
				.hasState(params.getAppState())
				.hasName(params.getAppName())
				.hasId(params.getAppId())
				.hasCredential(params.getCredential())
				.hasRedirectUrl(params.getRedirectUrl())
				.hasOrganizationName(params.getOrgName())
				.includeImage(false);
		if(params.getCredential()!=null || params.getRedirectUrl()!=null) builder.includeCredentials(true);
		return builder;
	}
}
