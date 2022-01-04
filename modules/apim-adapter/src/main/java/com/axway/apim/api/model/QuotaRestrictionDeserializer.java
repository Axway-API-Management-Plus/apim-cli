package com.axway.apim.api.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class QuotaRestrictionDeserializer extends JsonDeserializer<QuotaRestriction> {
	
	public enum DeserializeMode {apiManagerData, configFile};
	
	private final static String validPeriods = "month|week|day|hour|minute|second";
	
	private DeserializeMode desiralizeMode;
	
	APIManagerAPIAdapter apiAdapter;

	public QuotaRestrictionDeserializer(DeserializeMode desirializeMode) {
		super();
		this.desiralizeMode = desirializeMode;
		try {
			this.apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("serial")
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
		restriction.setMethod(node.get("method").asText());
		Map<String, String> configMap = new LinkedHashMap<String, String>();
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
			APIFilter apiFilter = new APIFilter.Builder().
					hasApiPath(node.get("apiPath").asText()).
					hasVHost(node.get("vhost").asText()).
					hasQueryStringVersion(node.get("apiRoutingKey").asText()).
					build();
			api = apiAdapter.getAPI(apiFilter, false);
			if(api == null) {
				throw new AppException("Invalid quota configuration. No API found using filter: '" + apiFilter + " (primary key: apiPath).", ErrorCode.INVALID_QUOTA_CONFIG);
			}
			restriction.setRestrictedAPI(api);
			restriction.setApiId(api.getId());
		// If no API-Path is given,
		} else if(node.has("api")) { // Field api might contain the API-ID (From API-Manager), the API-Name or a Star if restriction should be applied to all APIs.
			if("*".equals(node.get("api").asText())) {
				restriction.setApiId("*");
			} else {
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
			}
		// No API given, which means apply the Quota to all APIs
		} else {
			restriction.setApiId("*");
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
