package com.axway.apim.api.export.lib.params;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public class APIUpgradeAccessParams extends APIExportParams implements Parameters, APIFilterParams {

    private String referenceAPIId;
    private String referenceAPIName;
    private String referenceAPIVersion;
    private String referenceAPIOrganization;

    private boolean referenceAPIDeprecate;
    private boolean referenceAPIRetire;
    private long referenceAPIRetirementDate;

    private API referenceAPI;

    public API getReferenceAPI() {
        return referenceAPI;
    }

    public void setReferenceAPI(API referenceAPI) {
        this.referenceAPI = referenceAPI;
    }

    public boolean isReferenceAPIDeprecate() {
        return referenceAPIDeprecate;
    }

    public void setReferenceAPIDeprecate(boolean referenceAPIDeprecate) {
        this.referenceAPIDeprecate = referenceAPIDeprecate;
    }

    public boolean isReferenceAPIRetire() {
        return referenceAPIRetire;
    }

    public void setReferenceAPIRetire(boolean referenceAPIRetire) {
        this.referenceAPIRetire = referenceAPIRetire;
    }

    public long getReferenceAPIRetirementDate() {
        return referenceAPIRetirementDate;
    }

    public void setReferenceAPIRetirementDate(String referenceAPIRetirementDate) throws AppException {
        if (referenceAPIRetirementDate == null) return;
        this.referenceAPIRetirementDate = Utils.getParsedDate(referenceAPIRetirementDate);
    }

    public String getReferenceAPIId() {
        return referenceAPIId;
    }

    public void setReferenceAPIId(String referenceAPIId) {
        this.referenceAPIId = referenceAPIId;
    }

    public String getReferenceAPIName() {
        return referenceAPIName;
    }

    public void setReferenceAPIName(String referenceAPIName) {
        this.referenceAPIName = referenceAPIName;
    }

    public String getReferenceAPIVersion() {
        return referenceAPIVersion;
    }

    public void setReferenceAPIVersion(String referenceAPIVersion) {
        this.referenceAPIVersion = referenceAPIVersion;
    }

    public String getReferenceAPIOrganization() {
        return referenceAPIOrganization;
    }

    public void setReferenceAPIOrganization(String referenceAPIOrganization) {
        this.referenceAPIOrganization = referenceAPIOrganization;
    }

    public APIFilter getReferenceAPIFilter() {
        return new APIFilter.Builder()
            .hasApiId(getReferenceAPIId())
            .hasApiPath(getApiPath())
            .hasName(getReferenceAPIName())
            .hasVHost(getReferenceAPIVersion())
            .hasOrganization(getReferenceAPIOrganization())
            .hasState(API.STATE_PUBLISHED)
            .build();
    }

    @Override
    public void validateRequiredParameters() throws AppException {
        super.validateRequiredParameters();
        if (isReferenceAPIRetire() && getReferenceAPIRetirementDate() == 0) {
            throw new AppException("If API should be retired, a retirement date is required.", ErrorCode.MISSING_PARAMETER);
        }
    }
}
