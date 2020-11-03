package com.axway.apim.setup.remotehosts.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class RemoteHostsExportParams extends StandardExportParams {
	
	/** The name of the remote host  */
	private String name;
	
	private String id;
	
	public static synchronized RemoteHostsExportParams getInstance() {
		return (RemoteHostsExportParams)CoreParameters.getInstance();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
