package com.axway.apim.appexport.lib;

import java.util.Comparator;

import com.axway.apim.api.model.apps.ClientApplication;

public class ApplicationComparator implements Comparator<ClientApplication> {

	public ApplicationComparator() {
		super();
	}

	@Override
	public int compare(ClientApplication app1, ClientApplication app2) {
		if(app1==null || app2==null) return 0;
		if(app1.getName()==null || app2.getName()==null) return 0;
		return app1.getName().compareTo(app2.getName());
	}
}
