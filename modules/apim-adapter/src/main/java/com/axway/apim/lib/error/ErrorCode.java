package com.axway.apim.lib.error;

import com.axway.apim.lib.error.AppException.LogLevel;

public enum ErrorCode {

    SUCCESS(0, "Replication of the API was successful.", false, LogLevel.INFO),
    UNSUPPORTED_API_SPECIFICATION(1, "The API-Specification is not supported by the APIM-CLI.", false),
    CLI_PARSING(2, "Error in parsing input parameters", false, LogLevel.INFO),

    MISSING_PARAMETER(5, "There is a missing parameter.", false),
    INVALID_PARAMETER(6, "There is an invalid parameter.", false),
    API_ALREADY_EXISTS(7, "The API already exists for another organization.", false),
    BACKEND_API_DEF_NA(8, "Backend API definition is not available.", false),
    NO_CHANGE(10, "No change between desired and actual API has been detected.", false, LogLevel.WARN),
    EXPORT_FOLDER_EXISTS(12, "Export failed Export-Folder already exists.", false),
    UPDATE_ONLY_IS_SET(13, "Creating of a new API fails, when flag updateOnly is set.", false),
    BREAKING_CHANGE_DETECTED(15, "A breaking change detected, but no force flag was given.", false),
    NO_ADMIN_ROLE_USER(17, "An action failed due to a missing Admin-Account.", false),
    CANT_PARSE_HTTP_RESPONSE(20, "Cannot parse a HTTP-Response for REST-API-Call"),
    CANT_SEND_HTTP_REQUEST(25, "Cannot send a HTTP-Request."),
    CANT_READ_JSON_PAYLOAD(30, "Cannot read or parse JSON-Payload.", true),
    CANT_CREATE_API_PROXY(35, "Cannot create API-Proxy (FE-API)"),
    CANT_CREATE_BE_API(40, "Cannot import Swagger-Definition -> Can't create BE-API."),
    CANT_UPDATE_API_PROXY(45, "Can't update API-Proxy (FE-API)."),
    CANT_UPDATE_QUOTA_CONFIG(47, "Can't update Quota-Configuration."),
    CANT_UPDATE_API_STATUS(50, "Can't update the API-Status."),
    UNKNOWN_USER(51, "Unknown user given."),
    ERROR_CHANGEPASSWORD(52, "Error changing password of user."),
    QUERY_STRING_ROUTING_DISABLED(53, "Query-String routing option in API-Manager is not enabled", false),
    API_CONFIG_REQUIRES_QUERY_STRING(54, "The given API-Config needs a query string, as API-Manager has it enabled."),
    CANT_UPGRADE_API_ACCESS(55, "Can't upgrade API-Access."),
    UNKNOWN_API(56, "API is unknown / not found, etc.", false),
    UNKNOWN_ORGANIZATION(57, "Unknown organization given. Please check the API-Config-File.", false),
    UNKNOWN_APPLICATION(58, "Unknown application given. Please check the API-Config-File."),
    ACCESS_ORGANIZATION_ERR(59, "An error happens while managing organization permissions."),
    API_MANAGER_COMMUNICATION(60, "Generic API-Manager communication error.", false),
    INVALID_CLIENT_ORGANIZATIONS(61, "At least one configured organization is invalid. Check the log file for more information."),
    INVALID_CLIENT_APPLICATIONS(62, "At least one configured application is invalid. Check the log file for more information."),
    UNKNOWN_REMOTE_HOST(63, "Unknown remote host given.", false),
    NO_API_DEFINITION_CONFIGURED(64, "No API Specification configured.", false),
    CANT_READ_API_DEFINITION_FILE(65, "Can't read the given API-Definition", false),
    CANT_READ_WSDL_FILE(66, "Can't read the given WSDL-File."),
    API_MANAGER_LOGIN_FAILED(67, "Login to API-Manager failed."),
    NO_UNIQUE_REMOTE_HOST(68, "A unique remote host based on the given filter could not be found.", false),
    API_MANAGER_COMMUNICATION_ERR(69, "Unexpected API-Manager communication error.", true),
    CANT_READ_CONFIG_FILE(70, "Can't read the API Config-File.", false),
    INVALID_QUOTA_CONFIG(71, "The given Quota-Configuration is invalid.", false),
    API_OPERATION_NOT_FOUND(72, "The given operationId cannot be found.", false),
    REFERENCED_PROFILE_INVALID(73, "Invalid profile reference is used. For instance using a CORS-Profile in an InboundProfile.", false),
    UNSUPPORTED_FEATURE(75, "Unsupported feature.", false),
    BACKEND_BASEPATH_IS_INVALID(76, "The given backend basepath is not a valid URL."),
    CANT_CREATE_STATE_CHANGE(80, "If the Desired & Actual API can't be compared for any reason."),
    WRONG_KEYSTORE_PASSWORD(81, "The password for the keystore is wrong.", false),
    UNKNOWN_CUSTOM_POLICY(85, "A custom policy-name is unknown (Request-, Routing, ...)", false),
    CANT_SETUP_VHOST(87, "It wasn't possible to setup the V-Host for the FE-API"),
    CANT_CREATE_HTTP_CLIENT(88, "Cannot create HTTP client"),
    APP_NAME_IS_NOT_UNIQUE(89, "The given application name is not unique. More than one application has been found.", false),
    ERR_APPROVING_API(90, "API could not be approved.", false),
    ERR_DELETING_API(91, "API could not be deleted.", false),
    ERR_DELETING_ORG(92, "Organization could not be deleted.", false),
    ERR_GRANTING_ACCESS_TO_API(93, "Error granting access to an API.", false),
    ERR_EXPORTING_API_DAT_FILE(94, "Error exporting API-Date file.", false),
    ERR_CREATING_APPLICATION(95, "Error creating/updating an application.", false),
    ERR_PUBLISH_API(96, "API could not be changed to publish state", false),
    ERR_UNPUBSLISH_API(97, "API could not be changed to unpublish state", false),
    UNXPECTED_ERROR(99, "An unexpected error occurred.", true),
    CHECK_CERTS_UNXPECTED_ERROR(100, "There was an unexpected error checking the expiration date of certificates.", false),
    CHECK_CERTS_FOUND_CERTS(101, "Certificates found that will expire within the given number of days.", false),
    GRANT_ACCESS_APPLICATION_ERR(102, "Error granting application access to API."),
    REVOKE_ACCESS_APPLICATION_ERR(103, "Error revoking application access to API."),
    INVALID_SECURITY_PROFILE_CONFIG(104, "The given security profile is invalid.", false);


    private final int code;
    private final String description;
    private final Boolean printStackTrace;
    private final LogLevel logLevel;

    ErrorCode(int code, String description) {
        this(code, description, false, LogLevel.ERROR);
    }

    ErrorCode(int code, String description, Boolean printStackTrace) {
        this(code, description, printStackTrace, LogLevel.ERROR);
    }

    ErrorCode(int code, String description, Boolean printStackTrace, LogLevel logLevel) {
        this.code = code;
        this.description = description;
        this.printStackTrace = printStackTrace;
        this.logLevel = logLevel;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getPrintStackTrace() {
        return printStackTrace;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }
}
