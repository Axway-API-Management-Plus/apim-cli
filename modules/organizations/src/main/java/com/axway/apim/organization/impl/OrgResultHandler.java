package com.axway.apim.organization.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.apis.OrgFilter.Builder;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.organization.lib.OrgExportParams;

public abstract class OrgResultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(OrgResultHandler.class);

	public enum ResultHandler {
		JSON_EXPORTER(JsonOrgExporter.class),
		YAML_EXPORTER(YamlOrgExporter.class),
		CONSOLE_EXPORTER(ConsoleOrgExporter.class),
		ORG_DELETE_HANDLER(DeleteOrgHandler.class);
		
		private final Class<OrgResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ResultHandler(Class clazz) {
			this.implClass = clazz;
		}

		public Class<OrgResultHandler> getClazz() {
			return implClass;
		}
	}
	
	OrgExportParams params;
	ExportResult result;
	
	boolean hasError = false;
	
	public static OrgResultHandler create(ResultHandler exportImpl, OrgExportParams params, ExportResult result) throws AppException {
		try {
			Object[] intArgs = new Object[] { params, result };
			Constructor<OrgResultHandler> constructor =
					exportImpl.getClazz().getConstructor(OrgExportParams.class, ExportResult.class);
			return constructor.newInstance(intArgs);
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	protected OrgResultHandler(OrgExportParams params, ExportResult result) {
		this.params = params;
		this.result = result;
	}
	
	public abstract void export(List<Organization> apps) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getBaseOrgFilterBuilder() {
		return new Builder()
				.hasId(params.getId())
				.hasDevelopment(params.getDev())
				.includeCustomProperties(getCustomProperties())
				.hasName(params.getName());
	}
	
	protected List<String> getCustomProperties() {
		try {
			return APIManagerAdapter.getInstance().customPropertiesAdapter.getCustomPropertyNames(Type.organization);
		} catch (AppException e) {
			LOG.error("Error reading custom properties configuration for organization from API-Manager");
			return null;
		}
	}
	
	public abstract OrgFilter getFilter() throws AppException;
}
