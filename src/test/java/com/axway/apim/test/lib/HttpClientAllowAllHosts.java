package com.axway.apim.test.lib;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class HttpClientAllowAllHosts {

	@Bean
	public HttpClient httpClient() {			
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			
			sslContext.init(null, null, null);
			// We have to use deprecated stuff, as only the "DefaultHttpClient" can be used by class:
			// com.consol.citrus.http.client.BasicAuthClientHttpRequestFactory used to perform BasicAuth
			SSLSocketFactory sf = new SSLSocketFactory(sslContext);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme httpsScheme = new Scheme("https", 443, sf);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);
			
			// apache HttpClient version >4.2 should use BasicClientConnectionManager
			ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
			HttpClient httpClient = new DefaultHttpClient(cm);
			
			return httpClient;
			
		} catch (NoSuchAlgorithmException e) {
			throw new BeanCreationException("Failed to create http client for ssl connection", e);
		} catch (KeyManagementException e) {
			throw new BeanCreationException("Failed to create http client for ssl connection", e);
		}
	}
	
	@Bean
	public HttpComponentsClientHttpRequestFactory allowAllHostSslRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory(httpClient());
	}
}
