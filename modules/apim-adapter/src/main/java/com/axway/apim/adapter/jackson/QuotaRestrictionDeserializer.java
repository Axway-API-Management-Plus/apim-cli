package com.axway.apim.adapter.jackson;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionType;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuotaRestrictionDeserializer extends JsonDeserializer<QuotaRestriction> {

    public static final String PERIOD = "period";
    public static final String METHOD = "method";
    public static final String MESSAGES = "messages";

    public enum DeserializeMode {apiManagerData, configFile}

    private static final String VALID_PERIODS = "week|day|hour|minute|second";

    private final DeserializeMode deserializeMode;

    private final boolean addRestrictedAPI;

    APIManagerAPIAdapter apiAdapter;
    APIManagerAPIMethodAdapter apiMethodAdapter;

    public QuotaRestrictionDeserializer(DeserializeMode deserializeMode) {
        this(deserializeMode, true);
    }

    public QuotaRestrictionDeserializer(DeserializeMode deserializeMode, boolean addRestrictedAPI) {
        super();
        this.deserializeMode = deserializeMode;
        this.addRestrictedAPI = addRestrictedAPI;
        try {
            this.apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
            this.apiMethodAdapter = APIManagerAdapter.getInstance().getMethodAdapter();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QuotaRestriction deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        String type = node.get("type").asText();
        JsonNode quotaConfig = node.get("config");

        String period = quotaConfig.get(PERIOD).asText();
        String per = quotaConfig.get("per").asText();

        QuotaRestriction restriction = new QuotaRestriction();
        try {
            restriction.setType(QuotaRestrictionType.valueOf(type));
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid quota config. The restriction type: " + type + " is invalid.", ErrorCode.INVALID_QUOTA_CONFIG);
        }
        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.put(PERIOD, period);
        configMap.put("per", per);
        if (type.equals("throttle")) {
            configMap.put(MESSAGES, quotaConfig.get(MESSAGES).asText());
        } else {
            configMap.put("mb", quotaConfig.get("mb").asText());
        }
        restriction.setConfig(configMap);

        // Set the API, if any, this quota should be applied too
        // Different options can be used to configure the API (See: #145)

        // If an API-Path is given, it takes precedence and the API-Name and API-Version is completely ignored
        API api = null;
        if (node.has("apiPath")) { // Which might be given in the API-Config file
            if (addRestrictedAPI) {
                APIFilter apiFilter = new APIFilter.Builder().hasApiPath(node.get("apiPath").asText()).hasVHost(node.get("vhost") != null ? node.get("vhost").asText() : null).hasQueryStringVersion(node.get("apiRoutingKey") != null ? node.get("apiRoutingKey").asText() : null).build();
                api = apiAdapter.getAPI(apiFilter, false);
                if (api == null) {
                    throw new AppException("Invalid quota configuration. No API found using filter: '" + apiFilter + " (primary key: apiPath).", ErrorCode.INVALID_QUOTA_CONFIG);
                }
                restriction.setRestrictedAPI(api);
            }
            restriction.setApiId(api != null ? api.getId() : null);
            // If no API-Path is given,
        } else if (node.has("api")) { // Field api might contain the API-ID (From API-Manager), the API-Name or a Star if restriction should be applied to all APIs.
            if ("*".equals(node.get("api").asText())) {
                restriction.setApiId("*");
            } else {
                /*
                 * Sometime it is better not to attach the underlying API for performance reasons. For instance System- and Application-Quotas
                 * contain all APIs, but quite often only the specific API-Quota is required. Therefore the underlying API is added at a later point
                 */
                if (addRestrictedAPI) {
                    // If data comes from API-Manager api contains the API-ID
                    if (deserializeMode == DeserializeMode.apiManagerData) {
                        // api contains the ID of the API
                        api = APIManagerAdapter.getInstance().getApiAdapter().getAPIWithId(node.get("api").asText());
                        // In the API-Config file the api contains the apiName
                    } else if (deserializeMode == DeserializeMode.configFile) {
                        // If the apiPath is given, it takes precedence and the API-Name and API-Version is completely ignored
                        APIFilter apiFilter = new APIFilter.Builder().hasName(node.get("api").asText()).build();
                        api = apiAdapter.getAPI(apiFilter, false);
                        if (api == null) {
                            throw new AppException("Invalid quota configuration. No API found using API-Name: '" + node.get("api").asText() + " (Version: " + node.get("apiVersion").asText() + ").", ErrorCode.INVALID_QUOTA_CONFIG);
                        }

                    }
                    restriction.setRestrictedAPI(api);
                    restriction.setApiId(api != null ? api.getId() : null);
                } else {
                    // As a fall back we take over the ID we got and add it to the restriction
                    restriction.setApiId(node.get("api").asText());
                }
            }
            // No API given, which means apply the Quota to all APIs, which only makes sense for application quotas
        } else {
            restriction.setApiId("*");
        }
        if (!node.has(METHOD) || "*".equals(node.get(METHOD).asText())) {
            restriction.setMethod("*");
        } else {
            if (addRestrictedAPI) {
                // Specific method defined. Translate it into the methodId, but only for applications as otherwise it is related to the API anyway
                if (deserializeMode == DeserializeMode.configFile) {
                    APIMethod method = apiMethodAdapter.getMethodForName(restriction.getApiId(), node.get(METHOD).asText());
                    if (method == null) {
                        throw new AppException("Invalid quota configuration. Method: " + node.get(METHOD).asText() + " not found for API with ID: " + restriction.getApiId(), ErrorCode.INVALID_QUOTA_CONFIG);
                    }
                    restriction.setMethod(method.getId());
                } else if (deserializeMode == DeserializeMode.apiManagerData) {
                    // Take over the ID given by API-Manager is translated into method name in QuotaRestrictionSerializer
                    restriction.setMethod(node.get(METHOD).asText());
                }
            } else {
                // Take over the given method name
                restriction.setMethod(node.get(METHOD).asText());
            }
        }

        validateRestriction(restriction);
        return restriction;
    }

    private void validateRestriction(QuotaRestriction restriction) throws AppException {
        if (restriction.getType() == QuotaRestrictionType.throttlemb) {
            if (restriction.getConfig().get(PERIOD) == null || restriction.getConfig().get("per") == null || restriction.getConfig().get("mb") == null) {
                throw new AppException("Invalid quota config. For type 'throttlemb' the following configs are required: period, per, mb", ErrorCode.INVALID_QUOTA_CONFIG);
            }
        } else if (restriction.getType() == QuotaRestrictionType.throttle) {
            if (restriction.getConfig().get(PERIOD) == null || restriction.getConfig().get("per") == null || restriction.getConfig().get(MESSAGES) == null) {
                throw new AppException("Invalid quota config. For type 'throttle' the following configs are required: period, per, messages", ErrorCode.INVALID_QUOTA_CONFIG);
            }
        } else {
            throw new AppException("Unsupported Quota-Type: '" + restriction.getType() + "'. Must be either: throttle or throttlemb", ErrorCode.INVALID_QUOTA_CONFIG);
        }
        Pattern pattern = Pattern.compile("^(" + VALID_PERIODS + ")$");
        Matcher matcher = pattern.matcher(restriction.getConfig().get(PERIOD));
        if (!matcher.matches()) {
            throw new AppException("Invalid quota period: '" + restriction.getConfig().get(PERIOD) + "'. Must be one of the following: " + VALID_PERIODS, ErrorCode.INVALID_QUOTA_CONFIG);
        }
    }
}
