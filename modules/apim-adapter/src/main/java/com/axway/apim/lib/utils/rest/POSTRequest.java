package com.axway.apim.lib.utils.rest;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import com.axway.apim.lib.errorHandling.AppException;

public class POSTRequest extends RestAPICall {

	public POSTRequest(HttpEntity entity, URI uri) {
		super(entity, uri);
	}
	
	public POSTRequest(HttpEntity entity, URI uri, boolean useAdmin) {
		super(entity, uri, useAdmin);
	}

	@Override
	public HttpResponse execute() throws AppException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(entity);
		/*if(this.contentType!=null) {
			httpPost.setHeader("Content-type", this.contentType);
		}*/
		HttpResponse response = sendRequest(httpPost);
		return response;
	}
}
