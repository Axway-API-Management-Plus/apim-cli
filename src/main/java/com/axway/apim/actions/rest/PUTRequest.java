package com.axway.apim.actions.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.AppException;

public class PUTRequest extends RestAPICall {

	public PUTRequest(HttpEntity entity, URI uri, IResponseParser responseParser) {
		super(entity, uri, responseParser);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setEntity(entity);
		httpPut.setHeader("Content-type", this.contentType);
		HttpResponse response = sendRequest(httpPut);
		parseResponse(response);
		return response;
	}
}
