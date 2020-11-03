package com.axway.apim.api.export.lib.params;

import com.axway.apim.lib.Parameters;

public class APIApproveParams extends APIExportParams implements Parameters {
	
	private String publishVhost;

	public String getPublishVhost() {
		return publishVhost;
	}

	public void setPublishVhost(String publishVhost) {
		this.publishVhost = publishVhost;
	}
}
