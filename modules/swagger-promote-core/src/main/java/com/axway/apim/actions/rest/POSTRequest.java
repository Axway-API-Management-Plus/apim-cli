package com.axway.apim.actions.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.AppException;

public class POSTRequest extends RestAPICall {

	public POSTRequest(HttpEntity entity, URI uri, IResponseParser responseParser) {
		super(entity, uri, responseParser);
	}
	
	public POSTRequest(HttpEntity entity, URI uri, IResponseParser responseParser, boolean useAdmin) {
		super(entity, uri, responseParser, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(entity);
		if(this.contentType!=null) {
			httpPost.setHeader("Content-type", this.contentType);
		}
		HttpResponse response = sendRequest(httpPost);
		parseResponse(response);
		return response;
	}
}
