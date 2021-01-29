package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.api.model.OAuthClientProfile;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerOAuthClientProfilesAdapter {
	
	private static String CACHE_KEY = "OAUTH_CLIENT_CACHE_KEY";
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerOAuthClientProfilesAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	CoreParameters cmd = CoreParameters.getInstance();
	
	Cache<String, String> oauthClientCache = APIManagerAdapter.getCache(CacheType.oauthClientProviderCache, String.class, String.class);

	public APIManagerOAuthClientProfilesAdapter() {}
	
	String apiManagerResponse = null;
	
	private void readOAuthClientProfilesFromAPIManager() throws AppException {
		if(apiManagerResponse!=null) return;
		if(oauthClientCache.containsKey(CACHE_KEY)) this.apiManagerResponse = oauthClientCache.get(CACHE_KEY);
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/oauthclientprofiles").build();
			RestAPICall getRequest = new GETRequest(uri);
			httpResponse = getRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode != 200){
				throw new AppException("Can't get OAuth Client profiles from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			this.apiManagerResponse = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			oauthClientCache.put(CACHE_KEY, this.apiManagerResponse);
		} catch (URISyntaxException | UnsupportedOperationException | IOException e) {
			throw new AppException("Can't get OAuth Client profiles from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<OAuthClientProfile> getOAuthClientProfiles() throws AppException {
		readOAuthClientProfilesFromAPIManager();
		List<OAuthClientProfile> clientProfiles;
		try {
			clientProfiles = mapper.readValue(this.apiManagerResponse, new TypeReference<List<OAuthClientProfile>>(){});
		} catch (IOException e) {
			throw new AppException("Can't parse OAuth Client profiles returned from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return clientProfiles;
	}
	
	public OAuthClientProfile getOAuthClientProfile(String profileName) throws AppException {
		List<OAuthClientProfile> profiles = getOAuthClientProfiles();
		for(OAuthClientProfile profile : profiles) {
			if(profile.getName().equals(profileName)) {
				return profile;
			}
		}
		return null;
	}
	
	void setAPIManagerTestResponse(String response) {
		this.apiManagerResponse =  response;
	}
}
