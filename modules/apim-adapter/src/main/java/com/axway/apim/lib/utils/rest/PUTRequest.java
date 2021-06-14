package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

import com.axway.apim.lib.errorHandling.AppException;

public class PUTRequest extends RestAPICall {

	public PUTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}
	
	public PUTRequest(HttpEntity entity, URI uri, boolean useAdmin) {
		super(entity, uri, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setEntity(entity);
		//httpPut.setHeader("Content-type", this.contentType);
		HttpResponse response = sendRequest(httpPut);
		return response;
	}
}
