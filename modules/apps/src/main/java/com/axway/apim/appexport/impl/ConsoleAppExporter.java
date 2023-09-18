package com.axway.apim.appexport.impl;

import java.util.Arrays;
import java.util.List;

import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter.Builder;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleAppExporter extends ApplicationExporter {


    public static final String APPLICATION_ID = "Application-Id";
    public static final String NAME = "Name";
    public static final String STATE = "State";
    public static final String EMAIL = "Email";
    public static final String ENABLED = "Enabled";
    public static final String CREATED_BY = "Created by";
    Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

    public ConsoleAppExporter(AppExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<ClientApplication> apps) throws AppException {
        switch (params.getWide()) {
            case standard:
                printStandard(apps);
                break;
            case wide:
                printWide(apps);
                break;
            case ultra:
                printUltra(apps);
                break;
        }
    }

    private void printStandard(List<ClientApplication> apps) {
        Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
            new Column().header(APPLICATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(STATE).with(app -> app.getState().name()),
            new Column().header(EMAIL).with(ClientApplication::getEmail),
            new Column().header(ENABLED).with(app -> Boolean.toString(app.isEnabled())),
            new Column().header(CREATED_BY).with(app -> getCreatedBy(app.getCreatedBy(), app)
            ))));
    }

    private void printWide(List<ClientApplication> apps) {
        Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
            new Column().header(APPLICATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(STATE).with(app -> app.getState().name()),
            new Column().header(EMAIL).with(ClientApplication::getEmail),
            new Column().header(ENABLED).with(app -> Boolean.toString(app.isEnabled())),
            new Column().header(CREATED_BY).with(app -> getCreatedBy(app.getCreatedBy(), app)),
            new Column().header("Created on").with(app -> getCreatedOn(app.getCreatedOn()).toString()),
            new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(app -> app.getOrganization().getName())
        )));
    }

    private void printUltra(List<ClientApplication> apps) {
        Console.println(AsciiTable.getTable(borderStyle, apps, Arrays.asList(
            new Column().header(APPLICATION_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getId),
            new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(AbstractEntity::getName),
            new Column().header(STATE).with(app -> app.getState().name()),
            new Column().header(EMAIL).with(ClientApplication::getEmail),
            new Column().header(ENABLED).with(app -> Boolean.toString(app.isEnabled())),
            new Column().header(CREATED_BY).with(app -> getCreatedBy(app.getCreatedBy(), app)),
            new Column().header("Created on").with(app -> getCreatedOn(app.getCreatedOn()).toString()),
            new Column().header("Organization").dataAlign(HorizontalAlign.LEFT).with(app -> app.getOrganization().getName()),
            new Column().header("APIs").with(app -> Integer.toString(app.getApiAccess().size())),
            new Column().header("Quotas").with(app -> Boolean.toString(hasAppQuota(app))
            ))));
    }

    private boolean hasAppQuota(ClientApplication app) {
        return (app.getAppQuota() != null) &&
            app.getAppQuota().getRestrictions() != null &&
            !app.getAppQuota().getRestrictions().isEmpty();
    }

    @Override
    public ClientAppFilter getFilter() throws AppException {
        Builder builder = getBaseFilterBuilder();

        switch (params.getWide()) {
            case standard:
                builder.includeQuotas(false);
                if (params.getCredential() == null && params.getRedirectUrl() == null)
                    builder.includeCredentials(false);
                if (params.getApiName() == null) builder.includeAPIAccess(false);
                break;
            case wide:
                if (params.getCredential() == null && params.getRedirectUrl() == null)
                    builder.includeCredentials(false);
                builder.includeQuotas(false);
                builder.includeAPIAccess(false);
                break;
            case ultra:
                builder.includeQuotas(true);
                if (params.getCredential() == null && params.getRedirectUrl() == null) builder.includeCredentials(true);
                builder.includeAPIAccess(true);
                break;
        }
        return builder.build();
    }
}
