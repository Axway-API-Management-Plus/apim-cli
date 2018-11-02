package com.axway.apim.actions.rest;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPut;

public class PUTRequest extends RestAPICall {

	public PUTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}

	@Override
	public InputStream execute() {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setEntity(entity);
		httpPut.setHeader("Content-type", this.contentType);
		return sendRequest(httpPut);
	}
}
