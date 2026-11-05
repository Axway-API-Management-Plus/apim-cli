package com.axway.apim.lib.utils.rest;

import com.axway.apim.lib.error.AppException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpDelete;

import java.net.URI;

public class DELRequest extends RestAPICall {

	public DELRequest(URI uri) {
		super(null, uri);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpDelete httpDel = new HttpDelete(uri);
		return sendRequest(httpDel);
	}
}
