package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.setup.model.Quotas;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsolePrinterGlobalQuotas {

    APIManagerAdapter adapter;

    Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

    public ConsolePrinterGlobalQuotas() {
        try {
            adapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException("Unable to get APIManagerAdapter", e);
        }
    }

    public void export(Quotas quotas) throws AppException {
        Console.println();
        Console.println("Global Quotas for: '" + APIManagerAdapter.getInstance().getApiManagerName() + "' Version: " + APIManagerAdapter.getInstance().getApiManagerVersion());
        Console.println();
        printQuotas(quotas);
    }

    private void printQuotas(Quotas quotas) {
        Console.println("------------------------System Quota----------------------");
        List<QuotaRestriction> systemQuota = new ArrayList<>();
        systemQuota.add(quotas.getSystemQuota());
        printLineItem(systemQuota);
        Console.println("------------------------Application Quota-----------------");
        List<QuotaRestriction> applicationQuota = new ArrayList<>();
        applicationQuota.add(quotas.getApplicationQuota());
        printLineItem(applicationQuota);

    }

    private void printLineItem(List<QuotaRestriction> quotaRestrictions) {
        Console.println(AsciiTable.getTable(borderStyle, quotaRestrictions, Arrays.asList(
            new Column().header("API").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(QuotaRestriction::getApi),
            new Column().header("Method").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(QuotaRestriction::getMethod),
            new Column().header("Type").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(quotaRestriction -> quotaRestriction.getType().name()),
            new Column().header("mb").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(quotaRestriction -> quotaRestriction.getConfig().get("mb")),
            new Column().header("messages").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(quotaRestriction -> quotaRestriction.getConfig().get("messages")),
            new Column().header("period").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(quotaRestriction -> quotaRestriction.getConfig().get("period")),
            new Column().header("per").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(quotaRestriction -> quotaRestriction.getConfig().get("per"))
        )));
    }
}
