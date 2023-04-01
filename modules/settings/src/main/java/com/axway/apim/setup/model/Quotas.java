package com.axway.apim.setup.model;

import com.axway.apim.api.model.QuotaRestriction;

public class Quotas {

    private QuotaRestriction systemQuota;
    private QuotaRestriction applicationQuota;

    public QuotaRestriction getSystemQuota() {
        return systemQuota;
    }

    public void setSystemQuota(QuotaRestriction systemQuota) {
        this.systemQuota = systemQuota;
    }

    public QuotaRestriction getApplicationQuota() {
        return applicationQuota;
    }

    public void setApplicationQuota(QuotaRestriction applicationQuota) {
        this.applicationQuota = applicationQuota;
    }
}
