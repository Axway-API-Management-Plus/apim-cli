package com.axway.apim.appexport.lib;

import java.util.Comparator;

import com.axway.apim.api.model.APIAccess;

public class APIAccessComparator implements Comparator<APIAccess> {

	public APIAccessComparator() {
	}

	@Override
	public int compare(APIAccess apiAccess1, APIAccess apiAccess2) {
		if(apiAccess1==null || apiAccess2==null) return 0;
		if(apiAccess2.getApiName()==null || apiAccess2.getApiName()==null) return 0;
		int rc = apiAccess1.getApiName().compareTo(apiAccess2.getApiName());
		if(rc!=0) return rc; // If the name is different, the version doesn't matter
		// If one, doesn't have a version - it also doesn't matter
		if(apiAccess1.getApiVersion()==null || apiAccess2.getApiVersion()==null) return rc;
		// Next line isn't perfect and must be improved!
		return apiAccess1.getApiVersion().compareTo(apiAccess2.getApiVersion());
	}
}
