package com.axway.apim.actions.rest;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpGet;

public class GETRequest extends RestAPICall {

	public GETRequest(URI uri) {
		super(null, uri);
	}

	@Override
	public InputStream execute() {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Content-type", this.contentType);
		return sendRequest(httpGet);
	}
}
