package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.axway.apim.lib.error.AppException;

public class GETRequest extends RestAPICall {

	public GETRequest(URI uri) {
		super(null, uri);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpGet httpGet = new HttpGet(uri);
		return sendRequest(httpGet);
	}
}
