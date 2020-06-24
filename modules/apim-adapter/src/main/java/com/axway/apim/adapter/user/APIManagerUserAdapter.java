package com.axway.apim.adapter.user;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerUserAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerUserAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	Map<UserFilter, String> apiManagerResponse = new HashMap<UserFilter, String>();
	
	Cache<String, String> userCache;
	
	public APIManagerUserAdapter() {
		userCache = APIManagerAdapter.getCache(CacheType.userCache, String.class, String.class);
	}
	
	private void readUsersFromAPIManager(UserFilter filter) throws AppException {
		if(apiManagerResponse.get(filter) != null) return;
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Using OrgAdmin only to load users.");
		}
		String userId = "";
		// Specific user-id is requested
		if(filter.getId()!=null) {
			if(userCache.containsKey(filter.getId())) {
				apiManagerResponse.put(filter, userCache.get(filter.getId()));
				return;
			}
			userId = "/"+filter.getId();
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/users"+userId)
					.addParameters(filter.getFilters())
					.build();
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			LOG.debug("Load users from API-Manager using filter: " + filter);
			LOG.trace("Load users with URI: " + uri);
			httpResponse = getRequest.execute();
			if(httpResponse.getStatusLine().getStatusCode()!=HttpStatus.SC_OK) {
				LOG.error("Sent request: " + uri);
				LOG.error("Received Status-Code: " +httpResponse.getStatusLine().getStatusCode()+ ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
				throw new AppException("", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(!userId.equals("")) {
				// Store it as an Array
				response = "[" + response+ "]";
				apiManagerResponse.put(filter, response);
				userCache.put(userId, response);
			} else {
				// We got an Array from API-Manager
				apiManagerResponse.put(filter, response);
			}
		} catch (Exception e) {
			LOG.error("Error cant read users from API-Manager with filter: "+filter+". Can't parse response: " + httpResponse, e);
			throw new AppException("Error cant read users from API-Manager with filter: "+filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<User> getUsers(UserFilter filter) throws AppException {
		readUsersFromAPIManager(filter);
		try {
			List<User> allUsers = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<User>>(){});
			return allUsers;
		} catch (IOException e) {
			LOG.error("Error cant read users from API-Manager with filter: "+filter+". Returned response: " + apiManagerResponse);
			throw new AppException("Error cant read users from API-Manager with filter: "+filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public List<User> getAllUsers() throws AppException {
		return getUsers(new UserFilter.Builder().build());
	}
	
	public User getUserForLoginName(String loginName) throws AppException {
		User user = getUser(new UserFilter.Builder().hasLoginName(loginName).build());
		return user;
	}
	
	public User getUserForId(String userId) throws AppException {
		User user = getUser(new UserFilter.Builder().hasId(userId).build());
		return user;
	}
	
	public User getUser(UserFilter filter) throws AppException {
		List<User> users = getUsers(filter);
		if(users.size()>1) {
			throw new AppException("No unique user found", ErrorCode.UNKNOWN_USER);
		}
		if(users.size()==0) {
			LOG.info("No user found using filter: " + filter);
			return null;
		}
		return users.get(0);
	}
	
	public void setAPIManagerTestResponse(UserFilter key, String response) {
		this.apiManagerResponse.put(key, response);
	}
}
