package com.axway.apim.api.export.lib.params;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;

public class APIChangeParams extends APIExportParams implements Parameters {
	
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
