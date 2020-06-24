package com.axway.apim.api.export.lib;

import java.util.Comparator;

import com.axway.apim.api.API;

public class APIComparator implements Comparator<API> {

	public APIComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(API api1, API api2) {
		if(api1==null || api2==null) return 0;
		if(api1.getName()==null || api2.getName()==null) return 0;
		int rc = api1.getName().compareTo(api2.getName());
		if(rc!=0) return rc; // If the name is different, the version doesn't matter
		// If one, doesn't have a version - it also doesn't matter
		if(api1.getVersion()==null || api1.getVersion()==null) return rc;
		// Next line isn't perfect and must be improved!
		return api1.getVersion().compareTo(api2.getVersion());
	}
}
