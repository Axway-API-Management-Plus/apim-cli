package com.axway.apim.organization.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.OrgFilter.Builder;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.organization.lib.OrgExportParams;

public abstract class OrganizationExporter {
	
	protected static Logger LOG = LoggerFactory.getLogger(OrganizationExporter.class);
	
	public enum ExportImpl {
		JSON_EXPORTER(JsonOrgExporter.class),
		CONSOLE_EXPORTER(ConsoleOrgExporter.class);
		
		private final Class<OrganizationExporter> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ExportImpl(Class clazz) {
			this.implClass = clazz;
		}

		public Class<OrganizationExporter> getClazz() {
			return implClass;
		}
	}
	
	OrgExportParams params;
	
	boolean hasError = false;
	
	public static OrganizationExporter create(ExportImpl exportImpl, OrgExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<OrganizationExporter> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{OrgExportParams.class});
			OrganizationExporter exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public OrganizationExporter(OrgExportParams params) {
		this.params = params;
	}
	
	public abstract void export(List<Organization> apps) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getBaseOrgFilterBuilder() {
		Builder builder = new OrgFilter.Builder()
				.hasApiId(params.getValue("id"))
				.hasName(params.getValue("name"));
		return builder;
	}
	
	public abstract OrgFilter getFilter() throws AppException;
}
