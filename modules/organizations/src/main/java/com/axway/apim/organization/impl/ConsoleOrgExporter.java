package com.axway.apim.organization.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.organization.Constants;
import com.axway.apim.organization.lib.OrgExportParams;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleOrgExporter extends OrgResultHandler {


    APIManagerAdapter adapter;
    Map<String, Integer> apiCountPerOrg = null;
    Map<String, Integer> appCountPerOrg = null;

    Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

    public ConsoleOrgExporter(OrgExportParams params, ExportResult result) throws AppException {
        super(params, result);
        try {
            adapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new AppException("Unable to get APIManagerAdapter", ErrorCode.UNXPECTED_ERROR);
        }
    }

    @Override
    public void export(List<Organization> orgs) throws AppException {
        switch (params.getWide()) {
            case standard:
                printStandard(orgs);
                break;
            case wide:
                printWide(orgs);
                break;
            case ultra:
                printUltra(orgs);
        }
    }

    private void printStandard(List<Organization> orgs) {
        Console.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
            new Column().header(Constants.ORGANIZATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(Constants.NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(Constants.V_HOST).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(Organization::getVirtualHost),
            new Column().header(Constants.DEV).with(org -> Boolean.toString(org.isDevelopment())),
            new Column().header(Constants.EMAIL).with(Organization::getEmail),
            new Column().header(Constants.ENABLED).with(org -> Boolean.toString(org.isEnabled()))
        )));
    }

    private void printWide(List<Organization> orgs) {
        Console.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
            new Column().header(Constants.ORGANIZATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(Constants.NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(Constants.V_HOST).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(Organization::getVirtualHost),
            new Column().header(Constants.DEV).with(org -> Boolean.toString(org.isDevelopment())),
            new Column().header(Constants.EMAIL).with(Organization::getEmail),
            new Column().header(Constants.ENABLED).with(org -> Boolean.toString(org.isEnabled())),
            new Column().header("Created on").with(org -> new Date(org.getCreatedOn()).toString()),
            new Column().header("Restricted").with(org -> Boolean.toString(org.isRestricted()))
        )));
    }

    private void printUltra(List<Organization> orgs) {
        Console.println(AsciiTable.getTable(borderStyle, orgs, Arrays.asList(
            new Column().header(Constants.ORGANIZATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(Constants.NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(Constants.V_HOST).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(Organization::getVirtualHost),
            new Column().header(Constants.DEV).with(org -> Boolean.toString(org.isDevelopment())),
            new Column().header(Constants.EMAIL).with(Organization::getEmail),
            new Column().header(Constants.ENABLED).with(org -> Boolean.toString(org.isEnabled())),
            new Column().header("Created on").with(org -> new Date(org.getCreatedOn()).toString()),
            new Column().header("Restricted").with(org -> Boolean.toString(org.isRestricted())),
            new Column().header("APIs").with(this::getNoOfAPIsForOrg),
            new Column().header("Apps").with(this::getNoOfAppsForOrg)
        )));
    }

    @Override
    public OrgFilter getFilter() {
        return getBaseOrgFilterBuilder().build();
    }

    private String getNoOfAPIsForOrg(Organization org) {
        try {
            if (this.apiCountPerOrg == null) {
                this.apiCountPerOrg = new HashMap<>();
                List<API> allAPIs = adapter.getApiAdapter().getAPIs(new APIFilter.Builder().build(), false);
                for (API api : allAPIs) {
                    int count = this.apiCountPerOrg.get(api.getOrganization().getName()) == null ? 0 : this.apiCountPerOrg.get(api.getOrganization().getName());
                    count = count + 1;
                    this.apiCountPerOrg.put(api.getOrganization().getName(), count);
                }
            }
            return this.apiCountPerOrg.get(org.getName()) == null ? "N/A" : this.apiCountPerOrg.get(org.getName()).toString();
        } catch (AppException e) {
            return "Err";
        }
    }

    private String getNoOfAppsForOrg(Organization org) {
        try {
            if (this.appCountPerOrg == null) {
                this.appCountPerOrg = new HashMap<>();
                List<ClientApplication> allAPPs = adapter.getAppAdapter().getAllApplications(false);
                for (ClientApplication app : allAPPs) {
                    int count = this.appCountPerOrg.get(app.getOrganization().getName()) == null ? 0 : this.appCountPerOrg.get(app.getOrganization().getName());
                    count = count + 1;
                    this.appCountPerOrg.put(app.getOrganization().getName(), count);
                }
            }
            return this.appCountPerOrg.get(org.getName()) == null ? "N/A" : this.appCountPerOrg.get(org.getName()).toString();
        } catch (AppException e) {
            return "Err";
        }
    }
}
