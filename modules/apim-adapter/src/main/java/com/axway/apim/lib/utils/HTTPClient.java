package com.axway.apim.lib.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class HTTPClient implements AutoCloseable {
	
	private URI url;
	private String password;
	private String username;
	
	private CloseableHttpClient httpClient = null;
	
	private HttpClientContext clientContext;
	
	public HTTPClient(String url, String username, String password) throws AppException {
		try {
			this.url = new URI(url);
			this.password = password;
			this.username = username;
			getClient();
		} catch (URISyntaxException e) {
			throw new AppException("Error creating HTTP-Client.", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public void getClient() throws AppException {
		try {
			SSLContextBuilder builder = SSLContextBuilder.create();
			builder.loadTrustMaterial(null, new TrustAllStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
			HttpClientBuilder httpClientBuilder = HttpClients.custom()
					.setSSLSocketFactory(sslsf);
			
			if(this.username!=null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put( new HttpHost(url.getHost(), url.getPort(), url.getScheme()), basicAuth );
				clientContext = HttpClientContext.create();
				clientContext.setAuthCache(authCache);
				httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
			}
			this.httpClient = httpClientBuilder.build();
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			throw new AppException("Error creating HTTP-Client.", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public CloseableHttpResponse execute(HttpUriRequest request) throws Exception {
		return httpClient.execute(request, clientContext);
	}


	@Override
	public void close() throws Exception {
		try {
			this.httpClient.close();
		} catch (IOException e) {
		}
	}
}
