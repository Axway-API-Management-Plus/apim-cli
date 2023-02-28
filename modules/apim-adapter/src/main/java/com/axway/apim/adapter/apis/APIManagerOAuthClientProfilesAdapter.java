package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.api.model.OAuthClientProfile;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class APIManagerOAuthClientProfilesAdapter {
	
	private static final String CACHE_KEY = "OAUTH_CLIENT_CACHE_KEY";
	
	private static final Logger LOG = LoggerFactory.getLogger(APIManagerOAuthClientProfilesAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	private final CoreParameters cmd;
	
	Cache<String, String> oauthClientCache = APIManagerAdapter.getCache(CacheType.oauthClientProviderCache, String.class, String.class);

	public APIManagerOAuthClientProfilesAdapter() {
		cmd = CoreParameters.getInstance();
	}
	
	String apiManagerResponse = null;
	
	private void readOAuthClientProfilesFromAPIManager() throws AppException {
		if(apiManagerResponse!=null) return;
		if(oauthClientCache.containsKey(CACHE_KEY)) this.apiManagerResponse = oauthClientCache.get(CACHE_KEY);
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/oauthclientprofiles").build();
			RestAPICall getRequest = new GETRequest(uri);
			LOG.debug("Load OAuth-Profiles from API-Manager.");
			try(CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != 200) {
					throw new AppException("Can't get OAuth Client profiles from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				this.apiManagerResponse = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				oauthClientCache.put(CACHE_KEY, this.apiManagerResponse);
			}
		} catch (URISyntaxException | UnsupportedOperationException | IOException e) {
			throw new AppException("Can't get OAuth Client profiles from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
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
}
