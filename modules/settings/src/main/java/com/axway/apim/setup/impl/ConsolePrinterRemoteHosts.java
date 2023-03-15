package com.axway.apim.setup.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsolePrinterRemoteHosts {

	protected static Logger LOG = LoggerFactory.getLogger(ConsolePrinterRemoteHosts.class);
	
	APIManagerAdapter adapter;
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;
	
	private StandardExportParams params;

	public ConsolePrinterRemoteHosts(StandardExportParams params) {
		this.params = params;
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new RuntimeException("Unable to get APIManagerAdapter",e);
		}
	}

	public void export(Map<String, RemoteHost> remoteHosts) throws AppException {
		Console.println();
		Console.println("Remote hosts for: '" + APIManagerAdapter.getApiManagerName() + "' Version: " + APIManagerAdapter.getInstance().getApiManagerVersion());
		Console.println();
		switch(params.getWide()) {
		case standard:
			printStandard(remoteHosts.values());
			break;
		case wide:
			printWide(remoteHosts.values());
			break;
		case ultra:
			printUltra(remoteHosts.values());
		}
	}
	
	private void printStandard(Collection<RemoteHost> remoteHosts) {
		Console.println(AsciiTable.getTable(borderStyle, remoteHosts, Arrays.asList(
				new Column().header("Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getName),
				new Column().header("Port").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getPort())),
				new Column().header("Organization").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> remoteHost.getOrganization().getName()),
				new Column().header("Related APIs").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> getNumberOfRelatedAPIs(remoteHost.getName(), remoteHost.getPort()))
				)));
		printDetails(remoteHosts);
	}
	
	private void printWide(Collection<RemoteHost> remoteHosts) {
		Console.println(AsciiTable.getTable(borderStyle, remoteHosts, Arrays.asList(
				new Column().header("Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getName),
				new Column().header("Port").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getPort())),
				new Column().header("Organization").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> remoteHost.getOrganization().getName()),
				new Column().header("HTTP 1.1").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getAllowHTTP11())),
				new Column().header("Verify cert. hostname").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getVerifyServerHostname())),
				new Column().header("Send SNI").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getOfferTLSServerName())),
				new Column().header("Related APIs").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> getNumberOfRelatedAPIs(remoteHost.getName(), remoteHost.getPort()))
				)));
		printDetails(remoteHosts);
	}
	
	private void printUltra(Collection<RemoteHost> remoteHosts) {
		Console.println(AsciiTable.getTable(borderStyle, remoteHosts, Arrays.asList(
				new Column().header("Id").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getId),
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(RemoteHost::getName),
				new Column().header("Port").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getPort())),
				new Column().header("Organization").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> remoteHost.getOrganization().getName()),
				new Column().header("HTTP 1.1").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getAllowHTTP11())),
				new Column().header("Verify cert. hostname").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getVerifyServerHostname())),
				new Column().header("Send SNI").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Boolean.toString(remoteHost.getOfferTLSServerName())),
				new Column().header("Conn. TO").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getConnectionTimeout())),
				new Column().header("Active TO").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getActiveTimeout())),
				new Column().header("Trans. TO").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getTransactionTimeout())),
				new Column().header("Idle TO").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> Integer.toString(remoteHost.getIdleTimeout())),
				new Column().header("Related APIs").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(remoteHost -> getNumberOfRelatedAPIs(remoteHost.getName(), remoteHost.getPort()))
				)));
		printDetails(remoteHosts);
	}
	
	private void printDetails(Collection<RemoteHost> remoteHosts) {
		if(remoteHosts.size()!=1) return;
		RemoteHost remoteHost = remoteHosts.iterator().next();
		// If wide isn't ultra, we have to reload some more information for the detail view
		Console.println();
		Console.println("R E M O T E - H O S T  -  D E T A I L S");
		Console.println(String.format("%-30s", "Organization: ") + remoteHost.getOrganization().getName());
		Console.println(String.format("%-30s", "Created On: ") + new Date(remoteHost.getCreatedOn()));
		Console.println(String.format("%-30s", "Created By: ") + getCreatedBy(remoteHost));
		Console.println(String.format("%-30s", "Content-Length in request: ") + remoteHost.getIncludeContentLengthRequest());
		Console.println(String.format("%-30s", "Content-Length in response: ") + remoteHost.getIncludeContentLengthResponse());
		Console.println(String.format("%-30s", "Transaction timeout: ") + remoteHost.getTransactionTimeout());
		Console.println(String.format("%-30s", "Idle timeout: ") + remoteHost.getIdleTimeout());
		Console.println(String.format("%-30s", "Include correlation ID: ") + remoteHost.getExportCorrelationId());
		Console.println(String.format("%-30s", "Input Encodings: ") + Arrays.asList(remoteHost.getInputEncodings()));
		Console.println(String.format("%-30s", "Output Encodings: ") + Arrays.asList(remoteHost.getOutputEncodings()));
		Console.println(String.format("%-30s", "Related APIs (using the same backend): "));
		try {
			List<API> relatedAPIs = getRelatedAPIs(remoteHost.getName(), remoteHost.getPort());
			for(API api : relatedAPIs) {
				Console.printf("%-25s (%s)", api.getName(), api.getVersion());
			}
			if(relatedAPIs==null || relatedAPIs.size()==0) {
				Console.println("No API found with backend: '" + remoteHost.getName() + "' and port: " + remoteHost.getPort());
			}
		} catch (AppException e) {
			Console.println("ERR");
		}
	}
	
	private static String getNumberOfRelatedAPIs(String backendHost, Integer port) {
		try {
			return Integer.toString(getRelatedAPIs(backendHost, port).size());
		} catch (AppException e) {
			LOG.error("Error loading APIs related to Remote-Host with name: '"+backendHost+"' and port: " + port, e);
			return "Err";
		}
	}
	
	private static String getCreatedBy(RemoteHost remoteHost) {
		return (remoteHost.getCreatedBy()!=null) ? remoteHost.getCreatedBy().getName(): "N/A";
	}
	
	private static List<API> getRelatedAPIs(String backendHost, Integer port) throws AppException {
		String portFilter = String.valueOf(port);
		if(port==443 || port==80) {
			portFilter="";
		}
		APIFilter apiFilter = new APIFilter.Builder().hasBackendBasepath("*"+backendHost+"*"+portFilter).build();
		try {
			return APIManagerAdapter.getInstance().apiAdapter.getAPIs(apiFilter, true);
		} catch (AppException e) {
			throw e;
		}
	}
}
