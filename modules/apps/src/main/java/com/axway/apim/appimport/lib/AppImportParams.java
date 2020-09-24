package com.axway.apim.appimport.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardImportParams;

public class AppImportParams extends StandardImportParams {
	
	public static synchronized AppImportParams getInstance() {
		return (AppImportParams)CoreParameters.getInstance();
	}
	
	@Override
	public boolean isIgnoreCache() {
		// For import action we ignore the cache in all cases!
		return true;
	}
}
