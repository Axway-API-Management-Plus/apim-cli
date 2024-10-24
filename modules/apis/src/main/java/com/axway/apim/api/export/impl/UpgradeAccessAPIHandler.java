package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class UpgradeAccessAPIHandler extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeAccessAPIHandler.class);

    public UpgradeAccessAPIHandler(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        APIUpgradeAccessParams upgradeParams = (APIUpgradeAccessParams) params;
        API referenceAPI = upgradeParams.getReferenceAPI();
        if (referenceAPI == null) {
            throw new AppException("Reference API for upgrade is missing.", ErrorCode.UNKNOWN_API);
        }
        Console.println(apis.size() + " API(s) selected for upgrade based on reference/old API: " + referenceAPI.getName() + " " + referenceAPI.getVersion() + " (" + referenceAPI.getId() + ").");
        Console.println("Old/Reference API: deprecate: " + upgradeParams.isReferenceAPIDeprecate() + ", retired: " + upgradeParams.isReferenceAPIRetire() + ", retirementDate: " + getRetirementDate(upgradeParams.getReferenceAPIRetirementDate()));
        if (CoreParameters.getInstance().isForce()) {
            Console.println("Force flag given to upgrade: " + apis.size() + " API(s)");
        } else {
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
                Console.println("Okay, going to upgrade: " + apis.size() + " API(s) based on reference/old API: " + referenceAPI.getName() + " " + referenceAPI.getVersion() + " (" + referenceAPI.getId() + ").");
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        for (API api : apis) {
            try {
                if (APIManagerAdapter.getInstance().getApiAdapter().upgradeAccessToNewerAPI(api, referenceAPI,
                        upgradeParams.isReferenceAPIDeprecate(), upgradeParams.isReferenceAPIRetire(), upgradeParams.getReferenceAPIRetirementDate())) {
                    LOG.info("API: {} {} {} successfully upgraded.", api.getName(), api.getVersion(), api.getId());
                }
            } catch (Exception e) {
                LOG.error("Error upgrading API: {} {} {} Error message:{}", api.getName(), api.getVersion(), api.getId(), e.getMessage());
            }
        }
        Console.println("Done!");
    }

    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder();
        builder.hasState(API.STATE_PUBLISHED);
        return builder.build();
    }

    private String getRetirementDate(Long retirementDate) {
        if (retirementDate == null) return "N/A";
        Date retireDate = new Date(retirementDate);
        return DateFormat.getDateInstance(DateFormat.SHORT).format(retireDate);
    }

}
