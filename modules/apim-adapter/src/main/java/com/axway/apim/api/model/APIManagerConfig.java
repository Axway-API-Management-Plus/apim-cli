package com.axway.apim.api.model;

import com.axway.apim.lib.APIManagerConfigAnnotation;
import com.axway.apim.lib.APIManagerConfigAnnotation.ConfigType;

public class APIManagerConfig {
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "API Manager name",
			description = "The name of the API Manager.")
	private String portalName;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "API Manager host",
			description = "The network hostname or IP Address of the API Manager which will be used in email links.")
	private String portalHostname;

	@APIManagerConfigAnnotation(
			configType = ConfigType.APIPortal,
			name = "API Portal name",
			description = "The name of the API Portal")
	private String apiPortalName;

	@APIManagerConfigAnnotation(
			configType = ConfigType.APIPortal,
			name = "API Portal host and port",
			description = "The network hostname or IP Address of the API Portal which will be used in email links.")
	private String apiPortalHostname;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIPortal, 
			name = "API Portal",
			description = "Indicates if the API Portal is configured.")
	private Boolean isApiPortalConfigured;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "User registration",
			description = "Enables/disables user registration for the API Manager.")
	private Boolean registrationEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Enable password reset",
			description = "Enables/disables support for resetting user passwords for the API Manager.")
	private Boolean resetPasswordEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Change password on first login",
			description = "Enables/disables support for changing user passwords on first login for the API Manager.")
	private Boolean changePasswordOnFirstLogin;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Enable password expiry",
			description = "Enables/disables support for user passwords expiration for the API Manager.")
	private Boolean passwordExpiryEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Days before passwords expire",
			description = "The number of days before user passwords expire for the API Manager.")
	private Integer passwordLifetimeDays;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Minimum password length",
			description = "The minimum password length.")
	private Integer minimumPasswordLength;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Auto-approve user registration",
			description = "Enables/disables auto-approve for user registration whereby API Administrator or Organization Administrator approval is not required.")
	private Boolean autoApproveUserRegistration;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Enable application scopes",
			description = "Enables/disables the ability to add System scopes to an Application. These scopes represent Gateway OAuth resources that are not covered by APIs.")
	private Boolean systemOAuthScopesEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Apply application scope restrictions",
			description = "Enables/disables the ability to show Application default enabled scopes only.")
	private Boolean applicationScopeRestrictions;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Auto-approve applications",
			description = "Enables/disables auto-application approval whereby users do not need API Administrator or Organization Administrator approval.")
	private Boolean autoApproveApplications;

	@APIManagerConfigAnnotation(
			configType = ConfigType.Delegation,
			name = "Delegate user management",
			description = "Enables/disables user administration to the Organization Administrators.")
	private Boolean delegateUserAdministration;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Delegation,
			name = "Delegate application management",
			description = "Enables/disables application administration to the Organization Administrators.")
	private Boolean delegateApplicationAdministration;	
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIRegistration,
			name = "API default virtual host",
			description = "The network host and port that serves as the default virtual host from which API Manager registered API will be accessible through.")
	private String apiDefaultVirtualHost;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Enable query string version routing",
			description = "Enable routing to APIs on the same base path.")
	private Boolean apiRoutingKeyEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Query string version parameter",
			description = "An additional routing key is required to support multiple APIs registered on the same base path. This indicates where to look for the value.")
	private String apiRoutingKeyLocation;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "Email reply-to",
			description = "The 'from' address used in emails.")
	private String emailFrom;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "Email bounce",
			description = "An email address where undeliverable emails will be bounced to.")
	private String emailBounceAddress;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIRegistration,
			name = "API promotion via policy",
			description = "Enables/disables API promotion via policy.")
	private Boolean promoteApiViaPolicy;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.GlobalPolicies,
			name = "Enable Global Policies",
			description = "Enables/disables Global policies.")
	private Boolean globalPoliciesEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.GlobalPolicies,
			name = "Global request policy",
			description = "The Global Request Policy to be executed for all Frontend API calls. Must be a valid policy ID. Can be null to indicate no policy.")
	private String globalRequestPolicy;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.GlobalPolicies,
			name = "Global response policy",
			description = "The Global Response Policy to be executed for all Frontend API calls. Must be a valid policy ID. Can be null to indicate no policy.")
	private String globalResponsePolicy;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.FaultHandlers,
			name = "Enable API Manager fault handlers",
			description = "Enables/disables API Manager fault handlers.")
	private Boolean faultHandlersEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.FaultHandlers,
			name = "Global Fault Handler Policy",
			description = "The Global Fault Handler Policy to be used by all Frontend APIs in the event of an error. Must be a valid policy ID. Can be null to indicate no policy.")
	private String globalFaultHandlerPolicy;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIImport,
			name = "Strict certificate checking",
			description = "For API import, strict certificate checking will only allow recognised, valid server certificates when performing API imports from SSL protected endpoints. Default is set to true.")
	private Boolean strictCertificateChecking;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIImport,
			name = "Server certificate verification",
			description = "For API import, server certificate verification ensures that the certificate presented by the server matches the name of the remote host being connected to. Default is set to true.")
	private Boolean serverCertificateVerification;

	@APIManagerConfigAnnotation(
			configType = ConfigType.AdvisoryBanner,
			name = "Enable advisory banner",
			description = "Enables/disables API Manager advisory banner.")
	private Boolean advisoryBannerEnabled;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.AdvisoryBanner,
			name = "Advisory banner text.",
			description = "Advisory banner text.")
	private String advisoryBannerText;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Idle session timeout (minutes)",
			description = "Idle session timeout (minutes).")
	private Integer sessionIdleTimeout;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "Login name validation regex",
			description = "Login name validation regex")
	private String loginNameRegex;

	@APIManagerConfigAnnotation(
			configType = ConfigType.General,
			name = "User name validation regex",
			description = "User name validation regex")
	private String userNameRegex;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "Trial mode",
			description = "Trial mode")
	private Boolean isTrial;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "Default trial duration",
			description = "Default trial duration")
	private Integer defaultTrialDuration;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "API-Manager version",
			description = "The Version information of API Manager.")
	private String productVersion;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIManager,
			name = "API-Manager OS",
			description = "The operating system on which API Manager server is running.")
	private String os;
	
	/** The architecture of the operating system on which the API Manager server is running. Supported values: [ win-x86-32, linux-x86-64 ] */
	private String architecture;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIImport,
			name = "Import timeout (seconds)",
			description = "API Import timeout in seconds. Defaults to 60 seconds.")
	private Integer apiImportTimeout;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIImport,
			name = "Mime validation",
			description = "Specifies whether or not mime-type validation is performed during API Import. Currently mime-type validation is implemented for Swagger (1.x and 2.0) and OAS 3.0. This feature is switched on by default")
	private Boolean apiImportMimeValidation;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.APIImport,
			name = "Allow users to modify Backend APIs",
			description = "Specifies whether or not backend APIs can be edited post import. This option is switched OFF by default.")
	private Boolean apiImportEditable;
	
	@APIManagerConfigAnnotation(
			configType = ConfigType.Session,
			name = "Lock user account on invalid login",
			description = "Specifies whether or not to lock a user account when invalid login attempts are encountered; settings include, how many times authentication can be attempted, and what happens after this limit is crossed.")
	private LockUserAccount lockUserAccount;
	
	private Boolean baseOAuth;
	
	private Boolean oadminSelfServiceEnabled;
	
	private String externalUserName;
	
	private String externalUserDescription;
	
	private String externalUserPhone;
	
	private String externalUserEmail;
	
	private String externalUserOrganization;
	
	private String externalUserRole;
	
	private Boolean externalUserEnabled;

	public String getPortalName() {
		return portalName;
	}

	public void setPortalName(String portalName) {
		this.portalName = portalName;
	}

	public String getPortalHostname() {
		return portalHostname;
	}

	public void setPortalHostname(String portalHostname) {
		this.portalHostname = portalHostname;
	}

	public String getApiPortalName() {
		return apiPortalName;
	}

	public void setApiPortalName(String apiPortalName) {
		this.apiPortalName = apiPortalName;
	}

	public String getApiPortalHostname() {
		return apiPortalHostname;
	}

	public void setApiPortalHostname(String apiPortalHostname) {
		this.apiPortalHostname = apiPortalHostname;
	}

	public Boolean getIsApiPortalConfigured() {
		return isApiPortalConfigured;
	}

	public void setIsApiPortalConfigured(Boolean isApiPortalConfigured) {
		this.isApiPortalConfigured = isApiPortalConfigured;
	}

	public Boolean getRegistrationEnabled() {
		return registrationEnabled;
	}

	public void setRegistrationEnabled(Boolean registrationEnabled) {
		this.registrationEnabled = registrationEnabled;
	}

	public Boolean getResetPasswordEnabled() {
		return resetPasswordEnabled;
	}

	public void setResetPasswordEnabled(Boolean resetPasswordEnabled) {
		this.resetPasswordEnabled = resetPasswordEnabled;
	}

	public Boolean getChangePasswordOnFirstLogin() {
		return changePasswordOnFirstLogin;
	}

	public void setChangePasswordOnFirstLogin(Boolean changePasswordOnFirstLogin) {
		this.changePasswordOnFirstLogin = changePasswordOnFirstLogin;
	}

	public Boolean getPasswordExpiryEnabled() {
		return passwordExpiryEnabled;
	}

	public void setPasswordExpiryEnabled(Boolean passwordExpiryEnabled) {
		this.passwordExpiryEnabled = passwordExpiryEnabled;
	}

	public Integer getPasswordLifetimeDays() {
		return passwordLifetimeDays;
	}

	public void setPasswordLifetimeDays(Integer passwordLifetimeDays) {
		this.passwordLifetimeDays = passwordLifetimeDays;
	}

	public Integer getMinimumPasswordLength() {
		return minimumPasswordLength;
	}

	public void setMinimumPasswordLength(Integer minimumPasswordLength) {
		this.minimumPasswordLength = minimumPasswordLength;
	}

	public Boolean getAutoApproveUserRegistration() {
		return autoApproveUserRegistration;
	}

	public void setAutoApproveUserRegistration(Boolean autoApproveUserRegistration) {
		this.autoApproveUserRegistration = autoApproveUserRegistration;
	}

	public Boolean getSystemOAuthScopesEnabled() {
		return systemOAuthScopesEnabled;
	}

	public void setSystemOAuthScopesEnabled(Boolean systemOAuthScopesEnabled) {
		this.systemOAuthScopesEnabled = systemOAuthScopesEnabled;
	}

	public Boolean getApplicationScopeRestrictions() {
		return applicationScopeRestrictions;
	}

	public void setApplicationScopeRestrictions(Boolean applicationScopeRestrictions) {
		this.applicationScopeRestrictions = applicationScopeRestrictions;
	}

	public Boolean getAutoApproveApplications() {
		return autoApproveApplications;
	}

	public void setAutoApproveApplications(Boolean autoApproveApplications) {
		this.autoApproveApplications = autoApproveApplications;
	}

	public Boolean getDelegateUserAdministration() {
		return delegateUserAdministration;
	}

	public void setDelegateUserAdministration(Boolean delegateUserAdministration) {
		this.delegateUserAdministration = delegateUserAdministration;
	}

	public Boolean getDelegateApplicationAdministration() {
		return delegateApplicationAdministration;
	}

	public void setDelegateApplicationAdministration(Boolean delegateApplicationAdministration) {
		this.delegateApplicationAdministration = delegateApplicationAdministration;
	}

	public String getApiDefaultVirtualHost() {
		return apiDefaultVirtualHost;
	}

	public void setApiDefaultVirtualHost(String apiDefaultVirtualHost) {
		this.apiDefaultVirtualHost = apiDefaultVirtualHost;
	}

	public Boolean getApiRoutingKeyEnabled() {
		return apiRoutingKeyEnabled;
	}

	public void setApiRoutingKeyEnabled(Boolean apiRoutingKeyEnabled) {
		this.apiRoutingKeyEnabled = apiRoutingKeyEnabled;
	}

	public String getApiRoutingKeyLocation() {
		return apiRoutingKeyLocation;
	}

	public void setApiRoutingKeyLocation(String apiRoutingKeyLocation) {
		this.apiRoutingKeyLocation = apiRoutingKeyLocation;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailBounceAddress() {
		return emailBounceAddress;
	}

	public void setEmailBounceAddress(String emailBounceAddress) {
		this.emailBounceAddress = emailBounceAddress;
	}

	public Boolean getPromoteApiViaPolicy() {
		return promoteApiViaPolicy;
	}

	public void setPromoteApiViaPolicy(Boolean promoteApiViaPolicy) {
		this.promoteApiViaPolicy = promoteApiViaPolicy;
	}

	public Boolean getGlobalPoliciesEnabled() {
		return globalPoliciesEnabled;
	}

	public void setGlobalPoliciesEnabled(Boolean globalPoliciesEnabled) {
		this.globalPoliciesEnabled = globalPoliciesEnabled;
	}

	public String getGlobalRequestPolicy() {
		return globalRequestPolicy;
	}

	public void setGlobalRequestPolicy(String globalRequestPolicy) {
		this.globalRequestPolicy = globalRequestPolicy;
	}

	public String getGlobalResponsePolicy() {
		return globalResponsePolicy;
	}

	public void setGlobalResponsePolicy(String globalResponsePolicy) {
		this.globalResponsePolicy = globalResponsePolicy;
	}

	public Boolean getFaultHandlersEnabled() {
		return faultHandlersEnabled;
	}

	public void setFaultHandlersEnabled(Boolean faultHandlersEnabled) {
		this.faultHandlersEnabled = faultHandlersEnabled;
	}

	public String getGlobalFaultHandlerPolicy() {
		return globalFaultHandlerPolicy;
	}

	public void setGlobalFaultHandlerPolicy(String globalFaultHandlerPolicy) {
		this.globalFaultHandlerPolicy = globalFaultHandlerPolicy;
	}

	public Boolean getStrictCertificateChecking() {
		return strictCertificateChecking;
	}

	public void setStrictCertificateChecking(Boolean strictCertificateChecking) {
		this.strictCertificateChecking = strictCertificateChecking;
	}

	public Boolean getServerCertificateVerification() {
		return serverCertificateVerification;
	}

	public void setServerCertificateVerification(Boolean serverCertificateVerification) {
		this.serverCertificateVerification = serverCertificateVerification;
	}

	public Boolean getAdvisoryBannerEnabled() {
		return advisoryBannerEnabled;
	}

	public void setAdvisoryBannerEnabled(Boolean advisoryBannerEnabled) {
		this.advisoryBannerEnabled = advisoryBannerEnabled;
	}

	public String getAdvisoryBannerText() {
		return advisoryBannerText;
	}

	public void setAdvisoryBannerText(String advisoryBannerText) {
		this.advisoryBannerText = advisoryBannerText;
	}

	public Boolean getBaseOAuth() {
		return baseOAuth;
	}

	public void setBaseOAuth(Boolean baseOAuth) {
		this.baseOAuth = baseOAuth;
	}

	public Boolean getOadminSelfServiceEnabled() {
		return oadminSelfServiceEnabled;
	}

	public void setOadminSelfServiceEnabled(Boolean oadminSelfServiceEnabled) {
		this.oadminSelfServiceEnabled = oadminSelfServiceEnabled;
	}

	public String getExternalUserName() {
		return externalUserName;
	}

	public void setExternalUserName(String externalUserName) {
		this.externalUserName = externalUserName;
	}

	public String getExternalUserDescription() {
		return externalUserDescription;
	}

	public void setExternalUserDescription(String externalUserDescription) {
		this.externalUserDescription = externalUserDescription;
	}

	public String getExternalUserPhone() {
		return externalUserPhone;
	}

	public void setExternalUserPhone(String externalUserPhone) {
		this.externalUserPhone = externalUserPhone;
	}

	public String getExternalUserEmail() {
		return externalUserEmail;
	}

	public void setExternalUserEmail(String externalUserEmail) {
		this.externalUserEmail = externalUserEmail;
	}

	public String getExternalUserOrganization() {
		return externalUserOrganization;
	}

	public void setExternalUserOrganization(String externalUserOrganization) {
		this.externalUserOrganization = externalUserOrganization;
	}

	public String getExternalUserRole() {
		return externalUserRole;
	}

	public void setExternalUserRole(String externalUserRole) {
		this.externalUserRole = externalUserRole;
	}

	public Boolean getExternalUserEnabled() {
		return externalUserEnabled;
	}

	public void setExternalUserEnabled(Boolean externalUserEnabled) {
		this.externalUserEnabled = externalUserEnabled;
	}

	public Integer getSessionIdleTimeout() {
		return sessionIdleTimeout;
	}

	public void setSessionIdleTimeout(Integer sessionIdleTimeout) {
		this.sessionIdleTimeout = sessionIdleTimeout;
	}

	public Boolean getIsTrial() {
		return isTrial;
	}

	public void setIsTrial(Boolean isTrial) {
		this.isTrial = isTrial;
	}

	public Integer getDefaultTrialDuration() {
		return defaultTrialDuration;
	}

	public void setDefaultTrialDuration(Integer defaultTrialDuration) {
		this.defaultTrialDuration = defaultTrialDuration;
	}

	public String getLoginNameRegex() {
		return loginNameRegex;
	}

	public void setLoginNameRegex(String loginNameRegex) {
		this.loginNameRegex = loginNameRegex;
	}

	public String getUserNameRegex() {
		return userNameRegex;
	}

	public void setUserNameRegex(String userNameRegex) {
		this.userNameRegex = userNameRegex;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public Integer getApiImportTimeout() {
		return apiImportTimeout;
	}

	public void setApiImportTimeout(Integer apiImportTimeout) {
		this.apiImportTimeout = apiImportTimeout;
	}

	public Boolean getApiImportMimeValidation() {
		return apiImportMimeValidation;
	}

	public void setApiImportMimeValidation(Boolean apiImportMimeValidation) {
		this.apiImportMimeValidation = apiImportMimeValidation;
	}

	public Boolean getApiImportEditable() {
		return apiImportEditable;
	}

	public void setApiImportEditable(Boolean apiImportEditable) {
		this.apiImportEditable = apiImportEditable;
	}

	public LockUserAccount getLockUserAccount() {
		return lockUserAccount;
	}

	public void setLockUserAccount(LockUserAccount lockUserAccount) {
		this.lockUserAccount = lockUserAccount;
	}
}
