package com.axway.apim.lib.utils.rest;

import com.axway.apim.lib.errorHandling.AppException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;

import java.net.URI;

public class DELRequest extends RestAPICall {

	public DELRequest(URI uri) {
		super(null, uri);
	}
	
	public DELRequest(URI uri, boolean useAdmin) {
		super(null, uri, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpDelete httpDel = new HttpDelete(uri);
		return sendRequest(httpDel);
	}
}
