package com.axway.apim.api.export.lib.params;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;

public class APICheckCertificatesParams extends APIExportParams implements Parameters {
	
	private int numberOfDays;

	public static synchronized APICheckCertificatesParams getInstance() {
		return (APICheckCertificatesParams)CoreParameters.getInstance();
	}

	public int getNumberOfDays() {
		return numberOfDays;
	}


	public void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}
}
