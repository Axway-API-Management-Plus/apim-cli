package com.axway.apim.lib;

public enum ErrorCode {
	
	SUCCESS								(0,  "Replication of the API was successful."),
	MISSING_PARAMETER					(5,  "There is a missing parameter."),
	NO_CHANGE							(10, "No change between desired and actual API has been detected."),
	BREAKING_CHANGE_DETECTED			(15, "A breaking change detected, but no force flag was given."),
	NO_ADMIN_ROLE_USER					(17, "An action failed due to a missing Admin-Account."),
	CANT_PARSE_HTTP_RESPONSE			(20, "Cannot parse a HTTP-Response for REST-API-Call"),
	CANT_SEND_HTTP_REQUEST				(25, "Cannot send a HTTP-Request."),
	CANT_READ_JSON_PAYLOAD				(30, "Cannot read or parse JSON-Payload."),
	CANT_CREATE_API_PROXY				(35, "Cannot create API-Proxy (FE-API)"),
	CANT_CREATE_BE_API					(40, "Cannot import Swagger-Definition -> Can't create BE-API."),
	CANT_UPDATE_API_PROXY				(45, "Can't update API-Proxy (FE-API)."),
	CANT_UPDATE_QUOTA_CONFIG			(47, "Can't update Quota-Configuration."),
	CANT_UPDATE_API_STATUS				(50, "Can't update the API-Status."),
	QUERY_STRING_ROUTING_DISABLED		(53, "Query-String routing option in API-Manager is not enabled"),
	API_CONFIG_REQUIRES_QUERY_STRING	(54, "The given API-Config needs a query string, as API-Manager has it enabled."),
	CANT_UPGRADE_API_ACCESS				(55, "Can't upgrade API-Access."),
	UNKNOWN_ORGANIZATION				(57, "Unknown organization given. Please check the API-Config-File."),
	UNKNOWN_APPLICATION					(58, "Unknown application given. Please check the API-Config-File."),
	ACCESS_ORGANIZATION_ERR				(59, "An error happens while managing organization permissions."),
	API_MANAGER_COMMUNICATION			(60, "Generic API-Manager communication error."),
	INVALID_CLIENT_ORGANIZATION			(61, "At least one configured organization is invalid. Check the log file for more information."),
	INVALID_CLIENT_APPLICATION			(62, "At least one configured application is invalid. Check the log file for more information."),
	NO_API_DEFINITION_CONFIGURED		(64, "No API Definition configured."),
	CANT_READ_API_DEFINITION_FILE		(65, "Can't read the given Swagger-File."),
	CANT_READ_WSDL_FILE					(66, "Can't read the given WSDL-File."),
	CANT_READ_CONFIG_FILE				(70, "Can't read the API Config-File."),
	INVALID_QUOTA_CONFIG				(71, "The given Quota-Configuration is invalid."),
	API_OPERATION_NOT_FOUND				(72, "The given operationId cannot be found."),
	REFERENCED_PROFILE_INVALID			(73, "Invalid profile reference is used. For instance using a CORS-Profile in an InboundProfile."),
	UNSUPPORTED_FEATURE					(75, "Unsupported feature."),
	CANT_CREATE_STATE_CHANGE			(80, "If the Desired & Actual API can't be compared for any reason."),
	WRONG_KEYSTORE_PASSWORD				(81, "The password for the keystore is wrong."),
	UNKNOWN_CUSTOM_POLICY				(85, "A custom policy-name is unknown (Request-, Routing, ...)"),
	CANT_SETUP_VHOST					(87, "It wasn't possible to setup the V-Host for the FE-API"),
	UNXPECTED_ERROR						(99, "An unexpected error occured.");

	private final int code;
	private final String description;
	
	private ErrorCode(int code, String description) {
		this.code = code;
		this.description = description;
		
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
