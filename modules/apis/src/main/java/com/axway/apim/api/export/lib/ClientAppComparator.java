package com.axway.apim.api.export.lib;

import java.util.Comparator;

import com.axway.apim.api.model.apps.ClientApplication;

public class ClientAppComparator implements Comparator<ClientApplication> {

	public ClientAppComparator() { //default constructor
	}

	@Override
	public int compare(ClientApplication app1, ClientApplication app2) {
		if(app1==null || app2==null) return 0;
		if(app1.getName()==null || app2.getName()==null) return 0;
		return app1.getName().compareTo(app2.getName());
	}
}
