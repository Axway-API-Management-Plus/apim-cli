package com.axway.apim.actions.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
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

/**
 * Encapsulates logic to perform REST-API Calls to the API-Manager REST-API. For instance 
 * it's handling the CSRF-Tokens.</br>
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
		try {
			Transaction context = Transaction.getInstance();
			if(context.get("CSRF-Token")==null) {
				for (Header header : response.getAllHeaders()) {
					if(header.getName().equals("CSRF-Token")) {
						context.put("CSRF-Token", header.getValue());
						break;
					}
				}
			}
			if(this.reponseParser==null) return; 
			JsonNode lastReponse = reponseParser.parseResponse(response);
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
			Transaction context = Transaction.getInstance();
			if(context.get("CSRF-Token")!=null) {
				request.addHeader("CSRF-Token", ""+context.get("CSRF-Token"));
			}
			context.put("lastRequest", request);
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
