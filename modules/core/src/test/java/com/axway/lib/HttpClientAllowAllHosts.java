package com.axway.lib;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
			
			// set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			            public X509Certificate[] getAcceptedIssuers() {
			                    System.out.println("getAcceptedIssuers =============");
			                    return null;
			            }

			            public void checkClientTrusted(X509Certificate[] certs,
			                            String authType) {
			                    System.out.println("checkClientTrusted =============");
			            }

			            public void checkServerTrusted(X509Certificate[] certs,
			                            String authType) {
			                    System.out.println("checkServerTrusted =============");
			            }
			} }, new SecureRandom());
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
