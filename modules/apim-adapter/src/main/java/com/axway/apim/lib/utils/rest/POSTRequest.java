package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;

import com.axway.apim.lib.error.AppException;

public class POSTRequest extends RestAPICall {
	public POSTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(entity);
		return sendRequest(httpPost);
	}
}
