package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.errorHandling.AppException;

public class GETRequest extends RestAPICall {

	public GETRequest(URI uri, IResponseParser responseParser) {
		super(null, uri,responseParser);
	}
	
	public GETRequest(URI uri, IResponseParser responseParser, boolean useAdmin) {
		super(null, uri,responseParser, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Content-type", this.contentType);
		HttpResponse response = sendRequest(httpGet);
		parseResponse(response);
		return response;
	}
}
