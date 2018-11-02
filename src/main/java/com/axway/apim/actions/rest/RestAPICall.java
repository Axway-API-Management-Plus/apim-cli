package com.axway.apim.actions.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.axway.apim.actions.tasks.IResponseParser;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class RestAPICall {
	
	public final static String API_VERSION = "/api/portal/v1.3";
	
	protected HttpEntity entity;
	protected URI uri;
	
	protected HttpHost target;
	
	protected IResponseParser reponseParser;
	
	protected String contentType = "application/json";
	
	
	
	public RestAPICall(HttpEntity entity, URI uri) {
		super();
		this.entity = entity;
		this.uri = uri;
	}

	public abstract InputStream execute();
	
	public void registerResponseCallback(IResponseParser reponseParser) {
		this.reponseParser = reponseParser;
	}
	
	public void parseResponse(InputStream response) {
		JsonNode lastReponse = reponseParser.parseResponse(response);
		Transaction context = Transaction.getInstance();
		context.put("lastResponse", lastReponse);
	}
	
	protected InputStream sendRequest(HttpUriRequest request) {
		try {
			APIMHttpClient apimClient = APIMHttpClient.getInstance();
			CloseableHttpResponse response = apimClient.getHttpClient().execute(request, apimClient.getClientContext());
			return response.getEntity().getContent();
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
