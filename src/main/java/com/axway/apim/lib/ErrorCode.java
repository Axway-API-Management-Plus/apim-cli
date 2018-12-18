package com.axway.apim.lib;

public enum ErrorCode {
	
	SUCCESS						(0,  "Successful."),
	NO_CHANGE					(10, "No change detected."),
	CANT_PARSE_HTTP_RESPONSE	(20, "Cannot parse HTTP-Response for REST-API-Call"),
	CANT_SEND_HTTP_REQUEST		(25, "Cannot send HTTP-Request."),
	CANT_READ_JSON_PAYLOAD		(30, "Cannot read or parse JSON-Payload."),
	CANT_CREATE_API_PROXY		(35, "Cannot create API-Proxy (FE-API)"),
	CANT_CREATE_BE_API			(40, "Cannot import Swagger-Definition -> Can't create BE-API."),
	CANT_UPDATE_API_PROXY		(45, "Can't update API-Proxy."),
	CANT_UPDATE_QUOTA_CONFIG	(47, "Can't update Quota-Configuration."),
	CANT_UPDATE_API_STATUS		(50, "Can't update the API-Status."),
	CANT_UPGRADE_API_ACCESS		(55, "Can't upgrade API-Access"),
	API_MANAGER_COMMUNICATION	(60, "API-Manager communication error."),
	CANT_READ_SWAGGER_FILE		(65, "Can't read Swagger-File."),
	CANT_READ_CONFIG_FILE		(70, "Can't read Config-File."),
	UNSUPPORTED_FEATURE			(75, "Unsupported feature or operation used."),
	CANT_CREATE_STATE_CHANGE	(80, "Cannot use Import & Existing API to create change state."),
	UNKNOWN_CUSTOM_POLICY		(85, "A custom policy-name is unknown (Request-, Routing, ..."),
	CANT_SETUP_VHOST			(87, "It wasn't possible to setup the V-Host for the FE-API"),
	UNXPECTED_ERROR				(99, "An unexpected error occured.");

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
