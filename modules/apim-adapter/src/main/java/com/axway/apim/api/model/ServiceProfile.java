package com.axway.apim.api.model;

public class ServiceProfile {

	String apiId;
	
	String basePath;

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public String toString() {
		return "ServiceProfile [apiId=" + apiId + ", basePath=" + basePath + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceProfile other = (ServiceProfile) obj;

		// No need to compare apiId as desired state does not contain apiId
		if (basePath == null) {
			return other.basePath == null;
		} else return basePath.equals(other.basePath);
	}
}
