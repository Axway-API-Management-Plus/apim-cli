package com.axway.apim.organization.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardImportParams;

public class OrgImportParams extends StandardImportParams {
	
	public static synchronized OrgImportParams getInstance() {
		return (OrgImportParams)CoreParameters.getInstance();
	}
	
	@Override
	public boolean isIgnoreCache() {
		// For import action we ignore the cache in all cases!
		return true;
	}
}
