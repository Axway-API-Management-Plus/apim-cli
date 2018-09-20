package com.axway.apim;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);



	public static void main(String args[]) throws JsonProcessingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode configuration = objectMapper.readTree(new File("apim-config.json"));

		JsonNode apimConfig = configuration.get("apim");
		
		JsonNode apiConfig = configuration.get("api");

		String apiManagerURL = apimConfig.get("url").textValue();
		String username = apimConfig.get("username").textValue();
		String password = apimConfig.get("password").textValue();
		String orgName = apimConfig.findPath("development").textValue();
		
		String swaggerFileLocation = apiConfig.get("modelName").textValue();
		
		ArrayNode targets = (ArrayNode) apimConfig.findPath("target");

		log.info("API Manager URL {} Development Org Name {}", apiManagerURL, orgName);

		String apiName = apimConfig.get("name").textValue();
		String apiPath = apimConfig.get("path").textValue();

		String authType = apimConfig.get("url").textValue();
		String backendUsername = apimConfig.get("url").textValue();

		JsonNode authentication = apimConfig.get("authentication");

		AxwayClient app = new AxwayClient();
		InputStream inputStream = null;
		try {
			app.createConnection(apiManagerURL, username, password);
			URI uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/organizations/")
					.setParameter("field", "name").setParameter("op", "eq").setParameter("value", orgName).build();
			log.info("URL :" + uri.toString());
			inputStream = app.getRequest(uri);
			String orgId = null;
			try{
				orgId = JsonPath.parse(inputStream).read("$.[0].id", String.class);
			}catch (PathNotFoundException e) {
				log.info("Organization is not available exiting.....");
				System.exit(0);
			}
			
			log.info("Organization id {}" , orgId);
			

			uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/apirepo/import").build();

			log.info("Swagger Location : {}", swaggerFileLocation);
			// inputStream = app.postMultipart(orgId, swaggerURL,
			// uri.toString(), apiName);
			File file = new File(swaggerFileLocation);
			if (!file.exists()) {
				log.error("Swagger file is not available in the project");
				return;
			}
			log.info("Swagger file path :" + file.getAbsolutePath());
			inputStream = app.postMultipart(orgId, file, uri.toString(), apiName);

			String backendAPIId = JsonPath.parse(inputStream).read("$.id", String.class);
			String json = "{\"apiId\":\"" + backendAPIId + "\",\"organizationId\":\"" + orgId + "\"}";

			uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/proxies/").build();

			inputStream = app.postRequest(json, uri.toString());

			JsonNode jsonNode = objectMapper.readTree(inputStream);

			ArrayNode devices = (ArrayNode) ((ArrayNode) jsonNode.findPath("securityProfiles")).get(0).get("devices");

			String virtualAPIId = jsonNode.findPath("id").asText();

			devices.add(authentication);
			
			//Changing Frontend path
			((ObjectNode) jsonNode).put("path", "/" + apiPath);
			
			// Adding backend Authentication

			JsonNode auth = jsonNode.findPath("authenticationProfiles").get(0);
			((ObjectNode) auth).put("type", authType);
			JsonNode param = auth.get("parameters");
			((ObjectNode) param).put("username", backendUsername);
			((ObjectNode) param).put("password", "");

			uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/proxies/" + virtualAPIId).build();

			String jsonPayload = objectMapper.writeValueAsString(jsonNode);

			inputStream = app.putRequest(jsonPayload, uri.toString());
			String name = JsonPath.parse(inputStream).read("$.name", String.class);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("name", name));
			// publish
			log.info("publishing API");
			uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/proxies/" + virtualAPIId + "/publish")
					.build();
			inputStream = app.postRequest(formparams, uri.toString());

			log.info("API publish complete");
			
			if ( targets == null){
				return;
			}
			log.info("Granting access to Org");
			uri = new URIBuilder(apiManagerURL).setPath("/api/portal/v1.3/proxies/grantaccess").build();

//			formparams = new ArrayList<NameValuePair>();
//			formparams.add(new BasicNameValuePair("action", "orgs"));
//
//			formparams.add(new BasicNameValuePair("apiId", virtualAPIId));
//			
		//	fo
			
			//app.postRequest(formparams, uri.toString());
			//log.info("Granting access success");
			// Grant access

		} catch (KeyManagementException e) {
			log.error("Error {}", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error {}", e);
		} catch (KeyStoreException e) {
			log.error("Error {}", e);
		} catch (URISyntaxException e) {
			log.error("Error {}", e);
		} catch (ClientProtocolException e) {
			log.error("Error {}", e);
		} catch (IOException e) {
			log.error("Error {}", e);
		}

	}



}
