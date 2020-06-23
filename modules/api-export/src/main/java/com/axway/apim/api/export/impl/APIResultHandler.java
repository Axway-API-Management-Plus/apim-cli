package com.axway.apim.api.export.impl;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CUSTOM_PROP_TYPE;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIFilter.POLICY_TRANSLATION;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class APIResultHandler {

	protected static Logger LOG = LoggerFactory.getLogger(APIResultHandler.class);
	
	APIExportParams params;
	
	boolean hasError = false;
	
	public enum APIListImpl {
		JSON_EXPORTER(JsonAPIExporter.class),
		CONSOLE_EXPORTER(ConsoleAPIExporter.class),
		API_DELETE_HANDLER(DeleteAPIHandler.class),
		API_UNPUBLISH_HANDLER(UnpublishAPIHandler.class);
		
		private final Class<APIResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private APIListImpl(Class clazz) {
			this.implClass = clazz;
		}

		public Class<APIResultHandler> getClazz() {
			return implClass;
		}
	}

	public APIResultHandler(APIExportParams params) {
		this.params = params;
	}
	
	public static APIResultHandler create(APIListImpl exportImpl, APIExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<APIResultHandler> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{APIExportParams.class});
			APIResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public abstract void execute(List<API> apis) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public abstract APIFilter getFilter();
	
	protected Builder getBaseAPIFilterBuilder() {
		Builder builder = new APIFilter.Builder(APIType.CUSTOM)
				.hasVHost(params.getValue("vhost"))
				.hasApiPath(params.getValue("api-path"))
				.hasPolicyName(params.getValue("policy"))
				.hasId(params.getValue("id"))
				.hasName(params.getValue("name"))
				.hasState(params.getValue("state"))
				.hasBackendBasepath(params.getValue("backend"))
				.includeCustomProperties(APIManagerAdapter.getAllConfiguredCustomProperties(CUSTOM_PROP_TYPE.api))
				.translateMethods(METHOD_TRANSLATION.AS_NAME)
				.translatePolicies(POLICY_TRANSLATION.TO_NAME)
				.useAPIProxyAPIDefinition(params.isUseAPIProxyAPIDefinition());
		return builder;
	}
	
    protected static boolean askYesNo(String question) {
        return askYesNo(question, "[Y]", "[N]");
    }

    protected static boolean askYesNo(String question, String positive, String negative) {
        Scanner input = new Scanner(System.in);
        // Convert everything to upper case for simplicity...
        positive = positive.toUpperCase();
        negative = negative.toUpperCase();
        String answer;
        do {
            System.out.print(question);
            answer = input.next().trim().toUpperCase();
        } while (!answer.matches(positive) && !answer.matches(negative));
        input.close();
        // Assess if we match a positive response
        return answer.matches(positive);
    }
}
