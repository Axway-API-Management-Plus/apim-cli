package com.axway.apim.api.model;

import com.axway.apim.lib.APIManagerAlertsAnnotation;
import com.axway.apim.lib.APIManagerAlertsAnnotation.AlertType;

public class Alerts {
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "System quota exceeded")
    private Boolean runSysquotaExceeded;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "System quota exceed warning")
    private Boolean runSysquotaWarning;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "Application quota exceeded")
    private Boolean runAppquotaExceeded;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "Application quota exceed warning")
    private Boolean runAppquotaWarning;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "Application Default Quota changed")
    private Boolean quotaOverrideChanged;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "Application-specific quota removed")
    private Boolean quotaOverrideDeleted;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "Application-specific quota changed")
    private Boolean quotaDefaultChanged;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Quota,
			name = "System quota changed")
    private Boolean quotaSystemChanged;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Organization,
			name = "Organization created")
    private Boolean organizationCreate;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Organization,
			name = "Organization deleted")
    private Boolean organizationDelete;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Organization,
			name = "Organization enabled")
    private Boolean organizationEnable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Organization,
			name = "Organization disabled")
    private Boolean organizationDisable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.OrganizationAPIAccess,
			name = "API access created")
    private Boolean organizationAddapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.OrganizationAPIAccess,
			name = "API access deleted")
    private Boolean organizationRemoveapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.OrganizationAPIAccess,
			name = "API access enabled")
    private Boolean organizationEnableapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.OrganizationAPIAccess,
			name = "API access disabled")
    private Boolean organizationDisableapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer request created")
    private Boolean appdevRegister;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer request approved")
    private Boolean appdevApprove;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer deleted")
    private Boolean appdevDelete;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer enabled")
    private Boolean appdevEnable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer disabled")
    private Boolean appdevDisable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationDeveloper,
			name = "Developer reset password")
    private Boolean appdevResetpwd;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Application,
			name = "Application request created")
    private Boolean applicationRegister;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Application,
			name = "Application request approved")
    private Boolean applicationApprove;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Application,
			name = "Application deleted")
    private Boolean applicationDelete;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Application,
			name = "Application enabled")
    private Boolean applicationEnable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.Application,
			name = "Application disabled")
    private Boolean applicationDisable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationAPIAccess,
			name = "API access request created")
    private Boolean applicationRequestapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationAPIAccess,
			name = "API access request approved")
    private Boolean applicationApproveapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationAPIAccess,
			name = "API access deleted")
    private Boolean applicationRemoveapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationAPIAccess,
			name = "API access enabled")
    private Boolean applicationEnableapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationAPIAccess,
			name = "API access disabled")
    private Boolean applicationDisableapi;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APICatalog,
			name = "API enabled")
    private Boolean apicatalogEnable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APICatalog,
			name = "API disabled")
    private Boolean apicatalogDisable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APIRegistration,
			name = "Frontend API published")
    private Boolean apiproxyPublish;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APIRegistration,
			name = "Frontend API deprecated")
    private Boolean apiproxyDeprecate;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APIRegistration,
			name = "Frontend API undeprecated")
    private Boolean apiproxyUndeprecate;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APIRegistration,
			name = "Frontend API retired")
    private Boolean apiproxyRetire;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.APIRegistration,
			name = "Frontend API unpublished")
    private Boolean apiproxyUnpublish;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationCredentials,
			name = "Application credential created")
    private Boolean applicationcredentialsCreate;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationCredentials,
			name = "Application credential updated")
    private Boolean applicationcredentialsUpdate;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationCredentials,
			name = "Application credential deleted")
    private Boolean applicationcredentialsDelete;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationCredentials,
			name = "Application credential enabled")
    private Boolean applicationcredentialsEnable;
	
	@APIManagerAlertsAnnotation(
			alertType = AlertType.ApplicationCredentials,
			name = "Application credential disabled")
    private Boolean applicationcredentialsDisable;

	public Boolean getRunSysquotaExceeded() {
		return runSysquotaExceeded;
	}
	public void setRunSysquotaExceeded(Boolean runSysquotaExceeded) {
		this.runSysquotaExceeded = runSysquotaExceeded;
	}
	public Boolean getRunSysquotaWarning() {
		return runSysquotaWarning;
	}
	public void setRunSysquotaWarning(Boolean runSysquotaWarning) {
		this.runSysquotaWarning = runSysquotaWarning;
	}
	public Boolean getRunAppquotaExceeded() {
		return runAppquotaExceeded;
	}
	public void setRunAppquotaExceeded(Boolean runAppquotaExceeded) {
		this.runAppquotaExceeded = runAppquotaExceeded;
	}
	public Boolean getRunAppquotaWarning() {
		return runAppquotaWarning;
	}
	public void setRunAppquotaWarning(Boolean runAppquotaWarning) {
		this.runAppquotaWarning = runAppquotaWarning;
	}
	public Boolean getQuotaOverrideChanged() {
		return quotaOverrideChanged;
	}
	public void setQuotaOverrideChanged(Boolean quotaOverrideChanged) {
		this.quotaOverrideChanged = quotaOverrideChanged;
	}
	public Boolean getQuotaOverrideDeleted() {
		return quotaOverrideDeleted;
	}
	public void setQuotaOverrideDeleted(Boolean quotaOverrideDeleted) {
		this.quotaOverrideDeleted = quotaOverrideDeleted;
	}
	public Boolean getQuotaDefaultChanged() {
		return quotaDefaultChanged;
	}
	public void setQuotaDefaultChanged(Boolean quotaDefaultChanged) {
		this.quotaDefaultChanged = quotaDefaultChanged;
	}
	public Boolean getQuotaSystemChanged() {
		return quotaSystemChanged;
	}
	public void setQuotaSystemChanged(Boolean quotaSystemChanged) {
		this.quotaSystemChanged = quotaSystemChanged;
	}
	public Boolean getOrganizationCreate() {
		return organizationCreate;
	}
	public void setOrganizationCreate(Boolean organizationCreate) {
		this.organizationCreate = organizationCreate;
	}
	public Boolean getOrganizationDelete() {
		return organizationDelete;
	}
	public void setOrganizationDelete(Boolean organizationDelete) {
		this.organizationDelete = organizationDelete;
	}
	public Boolean getOrganizationEnable() {
		return organizationEnable;
	}
	public void setOrganizationEnable(Boolean organizationEnable) {
		this.organizationEnable = organizationEnable;
	}
	public Boolean getOrganizationDisable() {
		return organizationDisable;
	}
	public void setOrganizationDisable(Boolean organizationDisable) {
		this.organizationDisable = organizationDisable;
	}
	public Boolean getOrganizationAddapi() {
		return organizationAddapi;
	}
	public void setOrganizationAddapi(Boolean organizationAddapi) {
		this.organizationAddapi = organizationAddapi;
	}
	public Boolean getOrganizationRemoveapi() {
		return organizationRemoveapi;
	}
	public void setOrganizationRemoveapi(Boolean organizationRemoveapi) {
		this.organizationRemoveapi = organizationRemoveapi;
	}
	public Boolean getOrganizationEnableapi() {
		return organizationEnableapi;
	}
	public void setOrganizationEnableapi(Boolean organizationEnableapi) {
		this.organizationEnableapi = organizationEnableapi;
	}
	public Boolean getOrganizationDisableapi() {
		return organizationDisableapi;
	}
	public void setOrganizationDisableapi(Boolean organizationDisableapi) {
		this.organizationDisableapi = organizationDisableapi;
	}
	public Boolean getAppdevRegister() {
		return appdevRegister;
	}
	public void setAppdevRegister(Boolean appdevRegister) {
		this.appdevRegister = appdevRegister;
	}
	public Boolean getAppdevApprove() {
		return appdevApprove;
	}
	public void setAppdevApprove(Boolean appdevApprove) {
		this.appdevApprove = appdevApprove;
	}
	public Boolean getAppdevDelete() {
		return appdevDelete;
	}
	public void setAppdevDelete(Boolean appdevDelete) {
		this.appdevDelete = appdevDelete;
	}
	public Boolean getAppdevEnable() {
		return appdevEnable;
	}
	public void setAppdevEnable(Boolean appdevEnable) {
		this.appdevEnable = appdevEnable;
	}
	public Boolean getAppdevDisable() {
		return appdevDisable;
	}
	public void setAppdevDisable(Boolean appdevDisable) {
		this.appdevDisable = appdevDisable;
	}
	public Boolean getAppdevResetpwd() {
		return appdevResetpwd;
	}
	public void setAppdevResetpwd(Boolean appdevResetpwd) {
		this.appdevResetpwd = appdevResetpwd;
	}
	public Boolean getApplicationRegister() {
		return applicationRegister;
	}
	public void setApplicationRegister(Boolean applicationRegister) {
		this.applicationRegister = applicationRegister;
	}
	public Boolean getApplicationApprove() {
		return applicationApprove;
	}
	public void setApplicationApprove(Boolean applicationApprove) {
		this.applicationApprove = applicationApprove;
	}
	public Boolean getApplicationDelete() {
		return applicationDelete;
	}
	public void setApplicationDelete(Boolean applicationDelete) {
		this.applicationDelete = applicationDelete;
	}
	public Boolean getApplicationEnable() {
		return applicationEnable;
	}
	public void setApplicationEnable(Boolean applicationEnable) {
		this.applicationEnable = applicationEnable;
	}
	public Boolean getApplicationDisable() {
		return applicationDisable;
	}
	public void setApplicationDisable(Boolean applicationDisable) {
		this.applicationDisable = applicationDisable;
	}
	public Boolean getApplicationRequestapi() {
		return applicationRequestapi;
	}
	public void setApplicationRequestapi(Boolean applicationRequestapi) {
		this.applicationRequestapi = applicationRequestapi;
	}
	public Boolean getApplicationApproveapi() {
		return applicationApproveapi;
	}
	public void setApplicationApproveapi(Boolean applicationApproveapi) {
		this.applicationApproveapi = applicationApproveapi;
	}
	public Boolean getApplicationRemoveapi() {
		return applicationRemoveapi;
	}
	public void setApplicationRemoveapi(Boolean applicationRemoveapi) {
		this.applicationRemoveapi = applicationRemoveapi;
	}
	public Boolean getApplicationEnableapi() {
		return applicationEnableapi;
	}
	public void setApplicationEnableapi(Boolean applicationEnableapi) {
		this.applicationEnableapi = applicationEnableapi;
	}
	public Boolean getApplicationDisableapi() {
		return applicationDisableapi;
	}
	public void setApplicationDisableapi(Boolean applicationDisableapi) {
		this.applicationDisableapi = applicationDisableapi;
	}
	public Boolean getApicatalogEnable() {
		return apicatalogEnable;
	}
	public void setApicatalogEnable(Boolean apicatalogEnable) {
		this.apicatalogEnable = apicatalogEnable;
	}
	public Boolean getApicatalogDisable() {
		return apicatalogDisable;
	}
	public void setApicatalogDisable(Boolean apicatalogDisable) {
		this.apicatalogDisable = apicatalogDisable;
	}
	public Boolean getApiproxyPublish() {
		return apiproxyPublish;
	}
	public void setApiproxyPublish(Boolean apiproxyPublish) {
		this.apiproxyPublish = apiproxyPublish;
	}
	public Boolean getApiproxyDeprecate() {
		return apiproxyDeprecate;
	}
	public void setApiproxyDeprecate(Boolean apiproxyDeprecate) {
		this.apiproxyDeprecate = apiproxyDeprecate;
	}
	public Boolean getApiproxyUndeprecate() {
		return apiproxyUndeprecate;
	}
	public void setApiproxyUndeprecate(Boolean apiproxyUndeprecate) {
		this.apiproxyUndeprecate = apiproxyUndeprecate;
	}
	public Boolean getApiproxyRetire() {
		return apiproxyRetire;
	}
	public void setApiproxyRetire(Boolean apiproxyRetire) {
		this.apiproxyRetire = apiproxyRetire;
	}
	public Boolean getApiproxyUnpublish() {
		return apiproxyUnpublish;
	}
	public void setApiproxyUnpublish(Boolean apiproxyUnpublish) {
		this.apiproxyUnpublish = apiproxyUnpublish;
	}
	public Boolean getApplicationcredentialsCreate() {
		return applicationcredentialsCreate;
	}
	public void setApplicationcredentialsCreate(Boolean applicationcredentialsCreate) {
		this.applicationcredentialsCreate = applicationcredentialsCreate;
	}
	public Boolean getApplicationcredentialsUpdate() {
		return applicationcredentialsUpdate;
	}
	public void setApplicationcredentialsUpdate(Boolean applicationcredentialsUpdate) {
		this.applicationcredentialsUpdate = applicationcredentialsUpdate;
	}
	public Boolean getApplicationcredentialsDelete() {
		return applicationcredentialsDelete;
	}
	public void setApplicationcredentialsDelete(Boolean applicationcredentialsDelete) {
		this.applicationcredentialsDelete = applicationcredentialsDelete;
	}
	public Boolean getApplicationcredentialsEnable() {
		return applicationcredentialsEnable;
	}
	public void setApplicationcredentialsEnable(Boolean applicationcredentialsEnable) {
		this.applicationcredentialsEnable = applicationcredentialsEnable;
	}
	public Boolean getApplicationcredentialsDisable() {
		return applicationcredentialsDisable;
	}
	public void setApplicationcredentialsDisable(Boolean applicationcredentialsDisable) {
		this.applicationcredentialsDisable = applicationcredentialsDisable;
	}
}
