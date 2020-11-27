package com.axway.apim.organization.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class OrgExportParams extends StandardExportParams implements OrgFilterParams {
	
	private String name;
	private String id;
	private String dev;
	
	public static synchronized OrgExportParams getInstance() {
		return (OrgExportParams)CoreParameters.getInstance();
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

	public String getDev() {
		return dev;
	}

	public void setDev(String dev) {
		this.dev = dev;
	}
	
	
}
