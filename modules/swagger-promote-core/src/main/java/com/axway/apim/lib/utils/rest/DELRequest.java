package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.errorHandling.AppException;

public class DELRequest extends RestAPICall {

	public DELRequest(URI uri, IResponseParser responseParser) {
		super(null, uri, responseParser);
	}
	
	public DELRequest(URI uri, IResponseParser responseParser, boolean useAdmin) {
		super(null, uri, responseParser, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpUriRequest httpDel = new HttpDelete(uri);
		httpDel.setHeader("Content-type", this.contentType);
		HttpResponse response = sendRequest(httpDel);
		parseResponse(response);
		return response;
	}
}
