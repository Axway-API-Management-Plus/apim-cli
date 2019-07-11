package com.axway.apim.metadata.export.formats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.axway.apim.lib.AppException;
import com.axway.apim.metadata.export.beans.APIManagerExportMetadata;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.apiAccess.APIAccess;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;

public class AbstractReportFormat {
	
	protected APIManagerExportMetadata metaData;
	
	protected APIManagerAdapter mgrAdapater;
	
	protected void initApplicationAPISubcription() throws AppException {
		Map<String, IAPI> apisPerId = new HashMap<String, IAPI>();
		for(IAPI api : metaData.getAllAPIs()) {
			apisPerId.put(api.getId(), api);
		}
		for(ClientApplication clientApplication : metaData.getAllApps()) {
			// Additionally also get a List of APIs this application has a subscription to
			List<APIAccess> apiAccesses = APIManagerAdapter.getAPIAccess(clientApplication.getId(), "applications");
			// And for each subscription (APIAccess), map it to the API to be able to retrieve the applications per API
			for(APIAccess access : apiAccesses) {
				if(!access.getEnabled()) continue;
				String apiId = access.getApiId();
				List<ClientApplication> apps = apisPerId.get(apiId).getApplications();
				if(apps==null) {
					apps = new ArrayList<ClientApplication>();
					apisPerId.get(apiId).setApplications(apps);
				}
				apps.add(clientApplication);
			}
			clientApplication.setApiAccess(apiAccesses);
		}
	}
	protected Map<String, IAPI> getAPIMethods() throws AppException {
		Map<String, IAPI> APIsPerId = new HashMap<String, IAPI>();
		for(IAPI api : metaData.getAllAPIs()) {
			((ActualAPI)api).setApiMethods(mgrAdapater.getAllMethodsForAPI(api.getId()));
			APIsPerId.put(api.getId(), api);
		}
		return APIsPerId;
	}
	
	public void setMgrAdapater(APIManagerAdapter mgrAdapater) {
		this.mgrAdapater = mgrAdapater;
	}
	
	public APIManagerExportMetadata getMetaData() {
		return metaData;
	}
	public void setMetaData(APIManagerExportMetadata metaData) {
		this.metaData = metaData;
	}
	
	public static String beautifyPolicyName(String policyName) {
		String beautyName = "";
		String[] parts = policyName.split("<id field='name' value='");
		for(int i=1;i<parts.length;i++) {
			beautyName += "/" + parts[i].substring(0, parts[i].indexOf("'/>")).trim();
		}
		return beautyName;
	}
}
