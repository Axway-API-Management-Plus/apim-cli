package com.axway.apim.setup.impl;

import java.util.Arrays;
import java.util.List;

import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.error.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsolePrinterPolicies {

	private static final Logger LOG = LoggerFactory.getLogger(ConsolePrinterPolicies.class);

	APIManagerAdapter adapter;

	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsolePrinterPolicies() throws AppException {
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new AppException("Unable to get APIManagerAdapter", ErrorCode.UNXPECTED_ERROR);
		}
	}

	public void export(List<Policy> policies) throws AppException {
		Console.println();
		Console.println("Policies for: '" + APIManagerAdapter.getInstance().getApiManagerName() + "' Version: " + APIManagerAdapter.getInstance().getApiManagerVersion());
		Console.println();
		printPolicies(policies);
		Console.println("You may use 'apim api get -policy <PolicyName> -s api-env' to list all APIs using this policy");
	}

	private void printPolicies(List<Policy> policies) {
		Console.println(AsciiTable.getTable(borderStyle, policies, Arrays.asList(
				new Column().header("Policy-Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(Policy::getName),
				new Column().header("Type").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(policy -> policy.getType().getNiceName()),
				new Column().header("APIs").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(ConsolePrinterPolicies::getNumberOfRelatedAPIs)
				)));
	}

	private static String getNumberOfRelatedAPIs(Policy policy) {
		try {
			return Integer.toString(getRelatedAPIs(policy).size());
		} catch (AppException e) {
			LOG.error("Error loading APIs related for policy: "+policy, e);
			return "Err";
		}
	}

	private static List<API> getRelatedAPIs(Policy policy) throws AppException {
		APIFilter apiFilter = new APIFilter.Builder().hasPolicyName(policy.getName()).build();
		return APIManagerAdapter.getInstance().getApiAdapter().getAPIs(apiFilter, true);
	}
}
