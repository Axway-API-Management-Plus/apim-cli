package com.axway.apim.lib.utils.rest;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The interface to the API-Manager itself responsible to set up the underlying HTTPS-Communication.
 * It's used by the RESTAPICall.  
 * Implemented as a Singleton, which holds the actual connection to the API-Manager.
 * @author cwiechmann@axway.com
 *
 */
public class APIMHttpClient {
	
	private static Map<Boolean, APIMHttpClient> instances = new HashMap<>();
	
	private HttpClient httpClient;
	private HttpClientContext clientContext;
	private BasicCookieStore cookieStore = new BasicCookieStore();
	
	private String csrfToken;
	
	public static void deleteInstances() {
		instances = new HashMap<>();
	}
	
	public static void addInstance(boolean adminInstance, APIMHttpClient client) {
		instances.put(adminInstance, client);
	}
	
	public static APIMHttpClient getInstance() throws AppException {
		return getInstance(false);
	}
	
	public static APIMHttpClient getInstance(boolean adminInstance) throws AppException {
		if(!APIMHttpClient.instances.containsKey(adminInstance)) {
			APIMHttpClient client = new APIMHttpClient(adminInstance);
			instances.put(adminInstance, client);
		}
		return APIMHttpClient.instances.get(adminInstance);
	}
	
	private APIMHttpClient(boolean adminInstance) throws AppException {
		CoreParameters params = CoreParameters.getInstance();
		createConnection(params.getAPIManagerURL());
	}

	private void createConnection(URI uri) throws AppException {
		PoolingHttpClientConnectionManager httpClientConnectionManager;
		HttpHost targetHost;
		SSLContextBuilder builder = new SSLContextBuilder();
		try {
			builder.loadTrustMaterial(null, new TrustAllStrategy());

			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
	
			Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
					.register(uri.getScheme(), sslConnectionSocketFactory)
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.build();

			httpClientConnectionManager = new PoolingHttpClientConnectionManager(r);
			httpClientConnectionManager.setMaxTotal(5);
			httpClientConnectionManager.setDefaultMaxPerRoute(2);
			targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
	
			// Add AuthCache to the execution context
			clientContext = HttpClientContext.create();
			//clientContext.setAuthCache(authCache);
			clientContext.setCookieStore(cookieStore);
	
			httpClientConnectionManager.setMaxPerRoute(new HttpRoute(targetHost), 2);
			// We have make sure, that cookies are correclty parsed!
			RequestConfig.Builder defaultRequestConfig = RequestConfig.custom()
			        .setCookieSpec(CookieSpecs.STANDARD);
			CoreParameters params = CoreParameters.getInstance();

			HttpClientBuilder clientBuilder = HttpClientBuilder.create()
					.disableRedirectHandling()
					.setConnectionManager(httpClientConnectionManager)
					.useSystemProperties();

			// Check if a proxy is configured
			if(params.getProxyHost()!=null) {
				HttpHost proxyHost = new HttpHost(params.getProxyHost(), params.getProxyPort());
				HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
				clientBuilder.setRoutePlanner(routePlanner);
				if(params.getProxyUsername()!=null) {
					CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(params.getProxyHost(), params.getProxyPort()), new UsernamePasswordCredentials(params.getProxyUsername(), params.getProxyPassword()));
					clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
				defaultRequestConfig.setProxy(proxyHost);
			}
			clientBuilder.setDefaultRequestConfig(defaultRequestConfig.build());
			this.httpClient = clientBuilder.build();
		} catch (Exception e) {
			throw new AppException("Can't create connection to API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
		}
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public HttpClientContext getClientContext() {
		return clientContext;
	}

	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}

	@Override
	public String toString() {
		return "APIMHttpClient [cookieStore=" + cookieStore + ", csrfToken=" + csrfToken + "]";
	}
}
