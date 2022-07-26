package com.axway.apim.lib.utils.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

/**
 * Encapsulates logic to perform REST-API Calls to the API-Manager REST-API. For instance 
 * it's handling the CSRF-Tokens.  
 * More important is the implementation of ParseReponse. For every API-Call the implementing class 
 * can override the ParseResponse method, which can be used to validate the expected output.
 * 
 * @author cwiechmann@axway.com
 */
public abstract class RestAPICall {
	
	static Logger LOG = LoggerFactory.getLogger(RestAPICall.class);
	
	protected HttpEntity entity;
	protected URI uri;
	
	protected HttpHost target;
	
	protected boolean logHTTPClientInfo = false;
	
	protected boolean useAdmin = false;
	
	public RestAPICall(HttpEntity entity, URI uri, boolean useAdmin) {
		super();
		this.entity = entity;
		this.uri = uri;
		this.useAdmin = useAdmin;
	}
	
	public RestAPICall(HttpEntity entity, URI uri) {
		super();
		this.entity = entity;
		this.uri = uri;
	}

	public abstract HttpResponse execute() throws AppException;
	
	protected HttpResponse sendRequest(HttpUriRequest request) throws AppException {
		try {
			APIMHttpClient apimClient = APIMHttpClient.getInstance(this.useAdmin);
			if(logHTTPClientInfo) {
				LOG.info("Using APIM-Manager Client: " + apimClient.toString() + " ");
				LOG.info("Send request: "+this.getClass().getSimpleName()+" using admin-account: " + this.useAdmin + " to: " + request.getURI());
			}
			if(apimClient.getCsrfToken()!=null) request.addHeader("CSRF-Token", apimClient.getCsrfToken());
			HttpResponse response = apimClient.getHttpClient().execute(request, apimClient.getClientContext());
			return response;
		} catch (NoHttpResponseException e) {
			throw new AppException("No response received for request: "+request.toString()+" from API-Manager within time limit. "
					+ "Perhaps the API-Manager is overloaded or contains too many entities to process the request.", ErrorCode.UNXPECTED_ERROR, e);
		} catch (ClientProtocolException e) {
			throw new AppException("Unable to send HTTP-Request.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		} catch (IOException e) {
			throw new AppException("Unable to send HTTP-Request.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		}
	}

	public boolean isLogHTTPClientInfo() {
		return logHTTPClientInfo;
	}

	public void setLogHTTPClientInfo(boolean logHTTPClientInfo) {
		this.logHTTPClientInfo = logHTTPClientInfo;
	}
}
