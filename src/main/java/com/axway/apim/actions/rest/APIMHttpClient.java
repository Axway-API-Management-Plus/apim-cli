package com.axway.apim.actions.rest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;

/**
 * The interface to the API-Manager itself responsible to setup the underlying HTTPS-Communication. 
 * It's used by the RESTAPICall.</br>
 * Implemented as a Singleton, which holds the actual connection to the API-Manager.
 * @author cwiechmann@axway.com
 *
 */
public class APIMHttpClient {
	
	private static Map<Boolean, APIMHttpClient> instances = new HashMap<Boolean, APIMHttpClient>();
	
	private HttpClient httpClient;
	private HttpClientContext clientContext;
	
	private BasicCookieStore cookieStore = new BasicCookieStore();
	
	private String csrfToken;
	
	public static void deleteInstance() {
		instances = new HashMap<Boolean, APIMHttpClient>();
	}
	
	public static void addInstance(boolean adminInstance, APIMHttpClient client) {
		instances.put(adminInstance, client);
	}
	
	public static APIMHttpClient getInstance() throws AppException {
		return getInstance(false);
	}
	
	public static APIMHttpClient getInstance(boolean adminInstance) throws AppException {
		if(!APIMHttpClient.instances.containsKey(new Boolean(adminInstance))) {
			APIMHttpClient client = new APIMHttpClient(adminInstance);
			instances.put(new Boolean(adminInstance), client);
		}
		return APIMHttpClient.instances.get(new Boolean(adminInstance));
	}
	
	private APIMHttpClient(boolean adminInstance) throws AppException {
		CommandParameters params = CommandParameters.getInstance();
		if(adminInstance) {
			
			createConnection("https://"+params.getHostname()+":"+params.getPort());
		} else {
			createConnection("https://"+params.getHostname()+":"+params.getPort());
		}
	}

	private void createConnection(String apiManagerURL) throws AppException {
		PoolingHttpClientConnectionManager cm;
		HttpHost targetHost;
		
		URI uri = URI.create(apiManagerURL);
		SSLContextBuilder builder = new SSLContextBuilder();
		try {
			builder.loadTrustMaterial(null, new TrustAllStrategy());

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
	
			Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
					.register(uri.getScheme(), sslsf)
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.build();
	
			cm = new PoolingHttpClientConnectionManager(r);
			cm.setMaxTotal(5);
			cm.setDefaultMaxPerRoute(2);
			targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
	
			// Add AuthCache to the execution context
			clientContext = HttpClientContext.create();
			//clientContext.setAuthCache(authCache);
			clientContext.setCookieStore(cookieStore);
	
			cm.setMaxPerRoute(new HttpRoute(targetHost), 2);
			// We have make sure, that cookies are correclty parsed!
			RequestConfig defaultRequestConfig = RequestConfig.custom()
			        .setCookieSpec(CookieSpecs.STANDARD).build();
			
			this.httpClient = HttpClientBuilder.create()
					.disableRedirectHandling()
					.setConnectionManager(cm)
					.useSystemProperties()
					.setDefaultRequestConfig(defaultRequestConfig)
					.build();
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
}
