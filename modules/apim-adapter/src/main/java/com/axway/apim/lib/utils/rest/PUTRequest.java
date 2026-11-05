package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPut;

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
