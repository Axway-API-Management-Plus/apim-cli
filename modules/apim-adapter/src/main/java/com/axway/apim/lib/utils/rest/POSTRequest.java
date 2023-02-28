package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

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
