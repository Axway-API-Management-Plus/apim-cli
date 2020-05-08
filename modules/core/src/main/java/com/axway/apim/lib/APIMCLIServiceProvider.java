package com.axway.apim.lib;

public interface APIMCLIServiceProvider {
	
	String getName();
	
	String getVersion();
	
	String getDescription();
	
	String getGroupId();
	
	String getGroupDescription();
	
	String getMethod();
	
	int execute(String args[]);
}
