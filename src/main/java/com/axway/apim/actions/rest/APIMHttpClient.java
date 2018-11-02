package com.axway.apim.actions.rest;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import com.axway.apim.lib.CommandParameters;

public class APIMHttpClient {
	
	private static APIMHttpClient instance;
	
	private CloseableHttpClient httpClient;
	private HttpClientContext clientContext;
	
	public static APIMHttpClient getInstance() {
		if (APIMHttpClient.instance == null) {
			APIMHttpClient.instance = new APIMHttpClient();
		}
		return APIMHttpClient.instance;
	}
	
	private APIMHttpClient() {
		CommandParameters params = CommandParameters.getInstance();
		createConnection("https://"+params.getHostname()+":"+params.getPort(), params.getUsername(), params.getPassword());
	}

	private void createConnection(String apiManagerURL, String username, String password) {
		CredentialsProvider credsProvider;
		PoolingHttpClientConnectionManager cm;
		HttpHost targetHost;
		
		URI uri = URI.create(apiManagerURL);
		SSLContextBuilder builder = new SSLContextBuilder();
		try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
	
			Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
					.register(uri.getScheme(), sslsf).build();
	
			cm = new PoolingHttpClientConnectionManager(r);
			cm.setMaxTotal(5);
			cm.setDefaultMaxPerRoute(2);
			targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
			
			credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
					new UsernamePasswordCredentials(username, password));
	
			AuthCache authCache = new BasicAuthCache();
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(targetHost, basicAuth);
	
			// Add AuthCache to the execution context
			clientContext = HttpClientContext.create();
			clientContext.setAuthCache(authCache);
	
			cm.setMaxPerRoute(new HttpRoute(targetHost), 2);
			this.httpClient = HttpClientBuilder.create().setConnectionManager(cm)
					.setDefaultCredentialsProvider(credsProvider).build();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}
	
	public HttpClientContext getClientContext() {
		return clientContext;
	}
}
