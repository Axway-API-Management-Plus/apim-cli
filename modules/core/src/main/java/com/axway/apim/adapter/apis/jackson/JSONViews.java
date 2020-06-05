package com.axway.apim.adapter.apis.jackson;

public class JSONViews {
	
	public static class ApplicationForExport implements ApplicationBase, ApplicationAPIs, CredentialsBase, CredentialsExport, APIAccessForExport {}
	
	public static class ApplicationForAPIManager implements ApplicationBase, ApplicationAPIs {}
	
	public static class ApplicationForAPIManagerOnUpdate implements ApplicationBase {}
	
	public static class APIForExport implements APIBaseInformation, APIExportInformation {}
	
	public static class CredentialsForAPIManager implements CredentialsBase, CredentialsForManager {}
	
	public static interface ApplicationBase {}
	
	public static interface ApplicationAPIs {}
	
	public static interface CredentialsBase {}
	public static interface CredentialsForManager {}
	public static interface CredentialsExport {}
	
	public static interface APIAccessForAPIManager {}
	
	public static interface APIAccessForExport {}
	
	public static interface APIBaseInformation {}
	
	public static interface APIExportInformation {}
}
