package com.axway.apim.lib.utils.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	
	public final static String API_VERSION = "/api/portal/v1.3";
	
	protected HttpEntity entity;
	protected URI uri;
	
	protected HttpHost target;
	
	protected String contentType = "application/json";
	
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
			Transaction context = Transaction.getInstance();
			APIMHttpClient apimClient = APIMHttpClient.getInstance(this.useAdmin);
			if(apimClient.getCsrfToken()!=null) request.addHeader("CSRF-Token", apimClient.getCsrfToken());
			context.put("lastRequest", request.getMethod() + " " + request);
			HttpResponse response = apimClient.getHttpClient().execute(request, apimClient.getClientContext());
			//LOG.info("Send request: "+this.getClass().getSimpleName()+" using admin-account: " + this.useAdmin + " to: " + request.getURI());
			return response;
		} catch (ClientProtocolException e) {
			throw new AppException("Unable to send HTTP-Request.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		} catch (IOException e) {
			throw new AppException("Unable to send HTTP-Request.", ErrorCode.CANT_SEND_HTTP_REQUEST, e);
		}
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
