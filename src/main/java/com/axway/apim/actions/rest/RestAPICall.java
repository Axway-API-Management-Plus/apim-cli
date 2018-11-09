package com.axway.apim.actions.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.IResponseParser;
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

	public abstract HttpResponse execute();
	
	public void parseResponse(HttpResponse response) {
		if(this.reponseParser==null) return; 
		try {
			JsonNode lastReponse = reponseParser.parseResponse(response);
			Transaction context = Transaction.getInstance();
			context.put("lastResponse", lastReponse);
		} catch (Exception e) {
			try {
				RestAPICall.LOG.error("Response: '" + response.getStatusLine().toString() + "'");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
			throw new RuntimeException(e);
		}
			
			
	}
	
	protected CloseableHttpResponse sendRequest(HttpUriRequest request) {
		try {
			APIMHttpClient apimClient = APIMHttpClient.getInstance();
			CloseableHttpResponse response = apimClient.getHttpClient().execute(request, apimClient.getClientContext());
			return response;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
