package com.axway.apim.actions.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class RestAPICall {
	
	static Logger LOG = LoggerFactory.getLogger(RestAPICall.class);
	
	public final static String API_VERSION = "/api/portal/v1.3";
	
	protected HttpEntity entity;
	protected URI uri;
	
	protected HttpHost target;
	
	protected IResponseParser reponseParser;
	
	protected String contentType = "application/json";
	
	
	
	public RestAPICall(HttpEntity entity, URI uri, IResponseParser responseParser) {
		super();
		this.entity = entity;
		this.uri = uri;
		this.reponseParser = responseParser;
	}

	public abstract HttpResponse execute() throws AppException;
	
	public void parseResponse(HttpResponse response) throws AppException {
		if(this.reponseParser==null) return; 
		try {
			JsonNode lastReponse = reponseParser.parseResponse(response);
			Transaction context = Transaction.getInstance();
			context.put("lastResponse", lastReponse);
		} catch (Exception e) {
			try {
				RestAPICall.LOG.error("Response: '" + response.getStatusLine().toString() + "'");
			} catch (Exception e1) {
				throw new AppException("Unable to parse HTTP-Response", ErrorCode.CANT_PARSE_HTTP_RESPONSE, e1);
			}
			throw new AppException("Unable to parse HTTP-Response", ErrorCode.CANT_PARSE_HTTP_RESPONSE, e);
		}
			
			
	}
	
	protected HttpResponse sendRequest(HttpUriRequest request) throws AppException {
		try {
			APIMHttpClient apimClient = APIMHttpClient.getInstance();
			HttpResponse response = apimClient.getHttpClient().execute(request, apimClient.getClientContext());
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
