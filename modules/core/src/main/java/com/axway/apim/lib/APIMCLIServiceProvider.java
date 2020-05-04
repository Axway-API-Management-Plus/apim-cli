package com.axway.apim.lib;

public interface APIMCLIServiceProvider {
	
	String getDescription();
	
	String getId();
	
	String getMethod();
	
	int execute(String args[]);
}
