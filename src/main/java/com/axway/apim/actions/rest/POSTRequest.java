package com.axway.apim.actions.rest;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;

public class POSTRequest extends RestAPICall {

	public POSTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}

	@Override
	public InputStream execute() {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(entity);
		if(this.contentType!=null) {
			httpPost.setHeader("Content-type", this.contentType);
		}
		return sendRequest(httpPost);
	}
}
