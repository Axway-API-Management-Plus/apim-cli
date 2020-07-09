package com.axway.apim.users.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.adapter.user.UserFilter.Builder;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.users.lib.UserExportParams;

public abstract class UserResultHandler {
	
	protected static Logger LOG = LoggerFactory.getLogger(UserResultHandler.class);
	
	public enum ResultHandler {
		JSON_EXPORTER(JsonUserExporter.class),
		CONSOLE_EXPORTER(ConsoleUserExporter.class),
		ORG_DELETE_HANDLER(DeleteUserHandler.class);
		
		private final Class<UserResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ResultHandler(Class clazz) {
			this.implClass = clazz;
		}

		public Class<UserResultHandler> getClazz() {
			return implClass;
		}
	}
	
	UserExportParams params;
	
	boolean hasError = false;
	
	public static UserResultHandler create(ResultHandler exportImpl, UserExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<UserResultHandler> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{UserExportParams.class});
			UserResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public UserResultHandler(UserExportParams params) {
		this.params = params;
	}
	
	public abstract void export(List<User> users) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	protected Builder getBaseFilterBuilder() {
		Builder builder = new UserFilter.Builder()
				.hasId(params.getValue("id"))
				.hasLoginName(params.getLoginName())
				.hasName(params.getName())
				.hasOrganization(params.getOrg())
				.hasType(params.getType())
				.hasEmail(params.getEmail())
				.hasRole(params.getRole())
				.isEnabled(params.isEnabled());
		return builder;
	}
	
	public abstract UserFilter getFilter() throws AppException;
}
