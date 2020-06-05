package com.axway.apim.adapter.apis.jackson;

public class JSONViews {
	
	public static class ApplicationForExport implements ApplicationBaseInformation, ApplicationAPIs, CredentialsBaseInformation, CredentialsForExport, APIAccessForExport {}
	
	public static class ApplicationForAPIManager implements ApplicationBaseInformation, ApplicationForManager, ApplicationAPIs {}
	
	public static class ApplicationForAPIManagerOnUpdate implements ApplicationBaseInformation {}
	
	public static class APIForExport implements APIBaseInformation, APIExportInformation, ApplicationBaseInformation {}
	
	public static class CredentialsForAPIManager implements CredentialsBaseInformation, CredentialsForManager {}
	
	public static interface ApplicationBaseInformation {}
	public static interface ApplicationExportInformation {}
	public static interface ApplicationForManager {}
	public static interface ApplicationAPIs {}
	
	public static interface CredentialsBaseInformation {}
	public static interface CredentialsForManager {}
	public static interface CredentialsForExport {}
	
	public static interface APIAccessForAPIManager {}
	public static interface APIAccessForExport {}
	public static interface APIBaseInformation {}
	public static interface APIExportInformation {}
}
