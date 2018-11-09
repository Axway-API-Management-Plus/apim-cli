package com.axway.apim.actions.rest;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.axway.apim.actions.tasks.IResponseParser;

public class GETRequest extends RestAPICall {

	public GETRequest(URI uri, IResponseParser responseParser) {
		super(null, uri,responseParser);
	}

	@Override
	public HttpResponse execute() {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Content-type", this.contentType);
		HttpResponse response = sendRequest(httpGet);
		parseResponse(response);
		return response;
	}
}
