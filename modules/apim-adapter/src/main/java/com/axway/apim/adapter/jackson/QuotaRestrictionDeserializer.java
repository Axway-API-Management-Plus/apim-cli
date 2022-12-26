package com.axway.apim.adapter.jackson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class QuotaRestrictionDeserializer extends JsonDeserializer<QuotaRestriction> {
	
	public enum DeserializeMode {apiManagerData, configFile}

	private final static String validPeriods = "week|day|hour|minute|second";
	
	private DeserializeMode desiralizeMode;
	
	private boolean addRestrictedAPI = true;
	
	APIManagerAPIAdapter apiAdapter;
	APIManagerAPIMethodAdapter apiMethodAdapter;
	
	public QuotaRestrictionDeserializer(DeserializeMode deserializeMode) {
		this(deserializeMode, true);
	}

	public QuotaRestrictionDeserializer(DeserializeMode deserializeMode, boolean addRestrictedAPI) {
		super();
		this.desiralizeMode = deserializeMode;
		this.addRestrictedAPI = addRestrictedAPI;
		try {
			this.apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
			this.apiMethodAdapter = APIManagerAdapter.getInstance().methodAdapter;
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public QuotaRestriction deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);
		String type = node.get("type").asText();
		JsonNode quotaConfig = node.get("config");

		String period = quotaConfig.get("period").asText();
		String per = quotaConfig.get("per").asText();

		QuotaRestriction restriction = new QuotaRestriction();
		try {
			restriction.setType(QuotaRestrictiontype.valueOf(type));
		} catch (IllegalArgumentException e) {
			throw new AppException("Invalid quota config. The restriction type: " + type + " is invalid.", ErrorCode.INVALID_QUOTA_CONFIG);
		}
		Map<String, String> configMap = new LinkedHashMap<>();
		configMap.put("period", period);
		configMap.put("per", per);
		if(type.equals("throttle")) {
			configMap.put("messages", quotaConfig.get("messages").asText());
		} else {
			configMap.put("mb", quotaConfig.get("mb").asText());
		}
		restriction.setConfig(configMap);
		
		// Set the API, if any, this quota should be applied too		
		// Different options can be used to configure the API (See: #145)
		
		// If an API-Path is given, it takes precedence and the API-Name and API-Version is completely ignored
		API api = null;
		if(node.has("apiPath")) { // Which might be given in the API-Config file
			if(addRestrictedAPI) {
				APIFilter apiFilter = new APIFilter.Builder().
						hasApiPath(node.get("apiPath").asText()).
						hasVHost(node.get("vhost")!=null ? node.get("vhost").asText() : null).
						hasQueryStringVersion(node.get("apiRoutingKey")!=null ? node.get("apiRoutingKey").asText() : null).
						build();
				api = apiAdapter.getAPI(apiFilter, false);
				if(api == null) {
					throw new AppException("Invalid quota configuration. No API found using filter: '" + apiFilter + " (primary key: apiPath).", ErrorCode.INVALID_QUOTA_CONFIG);
				}
				restriction.setRestrictedAPI(api);
			}
			restriction.setApiId(api.getId());
		// If no API-Path is given,
		} else if(node.has("api")) { // Field api might contain the API-ID (From API-Manager), the API-Name or a Star if restriction should be applied to all APIs.
			if("*".equals(node.get("api").asText())) {
				restriction.setApiId("*");
			} else {
				/*
				 * Sometime it is better not to attach the underlying API for performance reasons. For instance System- and Application-Quotas
				 * contain all APIs, but quite often only the specific API-Quota is required. Therefore the underlying API is added at a later point
				 */
				if(addRestrictedAPI) {
					// If data comes from API-Manager api contains the API-ID
					if(desiralizeMode == DeserializeMode.apiManagerData) {
						// api contains the ID of the API
						api = APIManagerAdapter.getInstance().apiAdapter.getAPIWithId(node.get("api").asText());
					// In the API-Config file the api contains the apiName
					} else if (desiralizeMode == DeserializeMode.configFile) {
						// If the apiPath is given, it takes precedence and the API-Name and API-Version is completely ignored
						APIFilter apiFilter = new APIFilter.Builder().
								hasName(node.get("api").asText()).
								build();
						api = apiAdapter.getAPI(apiFilter, false);
						if(api == null) {
							throw new AppException("Invalid quota configuration. No API found using API-Name: '" + node.get("api").asText() + " (Version: "+node.get("apiVersion").asText()+").", ErrorCode.INVALID_QUOTA_CONFIG);
						}
						
					}
					restriction.setRestrictedAPI(api);
					restriction.setApiId(api.getId());
				} else {
					// As a fall back we take over the ID we got and add it to the restriction
					restriction.setApiId(node.get("api").asText());
				}
			}
		// No API given, which means apply the Quota to all APIs, which only makes sense for application quotas
		} else {
			restriction.setApiId("*");
		}
		if(!node.has("method") || "*".equals(node.get("method").asText())) {
			restriction.setMethod("*");
		} else {
			if(addRestrictedAPI) {
				// Specific method defined. Translate it into the methodId, but only for applications as otherwise it is related to the API anyway
				if(desiralizeMode == DeserializeMode.configFile) {
					APIMethod method = apiMethodAdapter.getMethodForName(restriction.getApiId(), node.get("method").asText());
					if(method == null) {
						throw new AppException("Invalid quota configuration. Method: "+node.get("method").asText()+" not found for API with ID: " +restriction.getApiId(), ErrorCode.INVALID_QUOTA_CONFIG);
					}
					restriction.setMethod(method.getId());
				} else if (desiralizeMode == DeserializeMode.apiManagerData) {
					// Take over the ID given by API-Manager is translated into method name in QuotaRestrictionSerializer
					restriction.setMethod(node.get("method").asText());
				}
			} else {
				// Take over the given method name
				restriction.setMethod(node.get("method").asText());
			}
		}
		
		validateRestriction(restriction);
		return restriction;
	}
	
	private void validateRestriction(QuotaRestriction restriction) throws AppException {
		if(restriction.getType()==QuotaRestrictiontype.throttlemb) {
			if(restriction.getConfig().get("period")==null || restriction.getConfig().get("per")==null || restriction.getConfig().get("mb")==null) {
				throw new AppException("Invalid quota config. For type 'throttlemb' the following configs are required: period, per, mb", ErrorCode.INVALID_QUOTA_CONFIG);
			}
		} else if(restriction.getType()==QuotaRestrictiontype.throttle) {
			if(restriction.getConfig().get("period")==null || restriction.getConfig().get("per")==null || restriction.getConfig().get("messages")==null) {
				throw new AppException("Invalid quota config. For type 'throttle' the following configs are required: period, per, messages", ErrorCode.INVALID_QUOTA_CONFIG);
			}
		} else {
			throw new AppException("Unsupported Quota-Type: '" + restriction.getType() + "'. Must be either: throttle or throttlemb", ErrorCode.INVALID_QUOTA_CONFIG);
		}
		
		
		Pattern pattern = Pattern.compile("^("+validPeriods+")$");
		Matcher matcher = pattern.matcher(restriction.getConfig().get("period"));
		if(!matcher.matches()) {
			throw new AppException("Invalid quota period: '"+restriction.getConfig().get("period")+"'. Must be one of the following: "+validPeriods, ErrorCode.INVALID_QUOTA_CONFIG);
		}
	}
}
