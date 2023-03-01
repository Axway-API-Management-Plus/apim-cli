package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

import com.axway.apim.lib.error.AppException;

public class PUTRequest extends RestAPICall {

	public PUTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}
	@Override
	public HttpResponse execute() throws AppException {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setEntity(entity);
		return sendRequest(httpPut);
	}
}
