package com.axway.apim.api.model;

import java.util.List;

public class WatchDog {
	
	List<ResponseCodes> responseCodes;
	
	HttpRequest httpRequest;
	
	Integer pollTimeout;
	
	Boolean pollIfUp;
	
	public List<ResponseCodes> getResponseCodes() {
		return responseCodes;
	}

	public void setResponseCodes(List<ResponseCodes> responseCodes) {
		this.responseCodes = responseCodes;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public Integer getPollTimeout() {
		return pollTimeout;
	}

	public void setPollTimeout(Integer pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

	public Boolean getPollIfUp() {
		return pollIfUp;
	}

	public void setPollIfUp(Boolean pollIfUp) {
		this.pollIfUp = pollIfUp;
	}

	static class ResponseCodes {
		Integer start;
		Integer end;
		
		public Integer getStart() {
			return start;
		}
		public void setStart(Integer start) {
			this.start = start;
		}
		public Integer getEnd() {
			return end;
		}
		public void setEnd(Integer end) {
			this.end = end;
		}
	}
	
	static class HttpRequest {
		String method;
		String uri;
		
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public String getUri() {
			return uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
	}
}
