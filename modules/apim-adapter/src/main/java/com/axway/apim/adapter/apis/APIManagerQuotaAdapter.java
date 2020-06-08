package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerQuotaAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerQuotaAdapter.class);
	
	public static enum Quota {
		SYSTEM_DEFAULT ("00000000-0000-0000-0000-000000000000"), 
		APPLICATION_DEFAULT ("00000000-0000-0000-0000-000000000001");
		
		private final String quotaId;
		
		private Quota(String quotaId) {
			this.quotaId = quotaId;
		}

		public String getQuotaId() {
			return quotaId;
		}
	}
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerQuotaAdapter() {}
	
	Map<String, String> apiManagerResponse = new HashMap<String, String>();
	
	private void readQuotaFromAPIManager(String quotaId) throws AppException {
		if(!APIManagerAdapter.hasAdminAccount()) return;
		if(this.apiManagerResponse.get(quotaId)!=null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {
			if(Quota.APPLICATION_DEFAULT.getQuotaId().equals(quotaId) || Quota.SYSTEM_DEFAULT.getQuotaId().equals(quotaId)) {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/quotas/"+quotaId).build();
			} else {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+quotaId+"/quota/").build();
			}
			RestAPICall getRequest = new GETRequest(uri, null, true);
			httpResponse = getRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode != 200){
				throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			this.apiManagerResponse.put(quotaId,EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
		} catch (URISyntaxException | UnsupportedOperationException | IOException e) {
			throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public APIQuota getQuotaForAPI(String quotaId, String apiId) throws AppException {
		readQuotaFromAPIManager(quotaId);
		APIQuota quotaConfig;
		try {
			quotaConfig = mapper.readValue(apiManagerResponse.get(quotaId), APIQuota.class);
			if(apiId!=null)
				quotaConfig = filterQuotaForAPI(quotaConfig, apiId);
		} catch (IOException e) {
			throw new AppException("Error cant load API-Methods for API: '"+apiId+"' from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return quotaConfig;
	}
	
	public APIQuota getDefaultQuota(Quota quotaType) throws AppException {
		readQuotaFromAPIManager(quotaType.getQuotaId());
		APIQuota quotaConfig;
		try {
			quotaConfig = mapper.readValue(apiManagerResponse.get(quotaType.getQuotaId()), APIQuota.class);
		} catch (IOException e) {
			throw new AppException("Error cant load default quotas from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return quotaConfig;
	}	
	
	private static APIQuota filterQuotaForAPI(APIQuota quotaConfig, String apiId) throws AppException {
		List<QuotaRestriction> apiRestrictions = new ArrayList<QuotaRestriction>();
		try {
			for(QuotaRestriction restriction : quotaConfig.getRestrictions()) {
				if(restriction.getApi().equals(apiId)) {
					apiRestrictions.add(restriction);
				}
			}
			if(apiRestrictions.size()==0) return null;
			APIQuota apiQuota = new APIQuota();
			apiQuota.setDescription(quotaConfig.getDescription());
			apiQuota.setName(quotaConfig.getName());
			apiQuota.setRestrictions(apiRestrictions);
			return apiQuota;
		} catch (Exception e) {
			throw new AppException("Can't parse quota from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	void setAPIManagerTestResponse(String apiId, String response) {
		this.apiManagerResponse.put(apiId, response);
	}
}
