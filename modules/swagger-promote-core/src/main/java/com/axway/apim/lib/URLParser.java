package com.axway.apim.lib;

public class URLParser {
	String username;
	
	String password;
	
	String uri;
	
	String urlToAPIDefinition;

	public URLParser(String urlToAPIDefinition) throws AppException {
		super();
		this.urlToAPIDefinition = urlToAPIDefinition;
		parseUrl();
	}
	
	private void parseUrl() throws AppException {
		if(!urlToAPIDefinition.contains("@")) {
			this.uri = urlToAPIDefinition;
		} else {
			try {
				String userNamePasswordPart = urlToAPIDefinition.substring(0, urlToAPIDefinition.lastIndexOf("@"));
				uri = urlToAPIDefinition.substring(urlToAPIDefinition.lastIndexOf("@")+1);
				username = userNamePasswordPart.substring(0, userNamePasswordPart.indexOf("/"));
				password = userNamePasswordPart.substring(userNamePasswordPart.indexOf("/")+1);
			} catch (Exception e) {
				throw new AppException("Can't parse given URL: '"+this.urlToAPIDefinition+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUri() {
		return uri;
	}
}
