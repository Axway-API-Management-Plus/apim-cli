package com.axway.apim.api.export.lib;

import com.axway.apim.lib.CoreParameters;

public class APIChangeParams extends APIExportParams {
	
	private String newBackend;
	
	private String oldBackend;

	public static synchronized APIChangeParams getInstance() {
		return (APIChangeParams)CoreParameters.getInstance();
	}

	public String getNewBackend() {
		return newBackend;
	}

	public void setNewBackend(String newBackend) {
		this.newBackend = newBackend;
	}

	public String getOldBackend() {
		return oldBackend;
	}

	public void setOldBackend(String oldBackend) {
		this.oldBackend = oldBackend;
	}
}
