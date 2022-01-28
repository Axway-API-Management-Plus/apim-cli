package com.axway.apim.api.definition; 

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.specification.DesiredAPISpecification;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.URLParser;
import com.axway.apim.lib.utils.Utils;

public class APISpecificationFactory {
	
	static Logger LOG = LoggerFactory.getLogger(APISpecificationFactory.class);
	
	private static ArrayList<Class<?>> specificationTypes = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 1L;

	{
	    add(Swagger2xSpecification.class);
	    add(Swagger1xSpecification.class);
	    add(OAS3xSpecification.class);
	    add(WSDLSpecification.class);
	    add(WADLSpecification.class);
	    add(ODataV2Specification.class);
	    add(ODataV3Specification.class);
	    add(ODataV4Specification.class);
	}};
	
	public static APISpecification getAPISpecification(DesiredAPISpecification desiredAPISpec, String configBaseDir, String apiName) throws AppException {
		return getAPISpecification(getAPIDefinitionContent(desiredAPISpec.getResource(), configBaseDir), desiredAPISpec.getResource(), apiName, true, true);
	}
	
	/**
	 * @param parsedAPISpecificationContent the content API-Specification itself
	 * @param apiDefinitionFile the path to the file that contains the spec. It is also used to determine the specification type
	 * @param apiName name of the API. Used for error reporting only
	 * @return the detected concrete API-Specification
	 * @throws AppException if the API-Specification cannot be detected or loaded
	 */
	public static APISpecification getAPISpecification(String apiDefinitionFile, String configBaseDir, String apiName) throws AppException {
		return getAPISpecification(getAPIDefinitionContent(apiDefinitionFile, configBaseDir), apiDefinitionFile, apiName, true, true);
	}
	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String apiName) throws AppException {
		return getAPISpecification(apiSpecificationContent, apiDefinitionFile, apiName, true, true);
	}
	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String configBaseDir, String apiName, String apiDefinitionFile) throws AppException {
		return getAPISpecification(apiSpecificationContent, apiDefinitionFile, apiName, true, true);
	}
	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String apiName, boolean failOnError, boolean logDetectedVersion) throws AppException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Handle API-Specification: '" + getContentStart(apiSpecificationContent) + "...', apiDefinitionFile: '"+apiDefinitionFile+"'");	
		}
		for(Class clazz : specificationTypes) {
			try {				
	            Constructor<?> constructor = clazz.getDeclaredConstructor();
				APISpecification spec = (APISpecification) constructor.newInstance();
				spec.setApiSpecificationFile(apiDefinitionFile);
				if(!spec.parse(apiSpecificationContent)) {
					LOG.debug("Can't handle API specification with class: " + clazz.getName());
					continue;
				} else {
					String addNote = "";
					if(spec.getAPIDefinitionType().getAdditionalNote()!=null) {
						addNote = "\n                                 | " + spec.getAPIDefinitionType().getAdditionalNote();
					}
					if(logDetectedVersion) {
						LOG.info("Detected: " + spec.getAPIDefinitionType().niceName + " specification. " + spec.getAPIDefinitionType().getNote()+addNote);
					}
					return spec;
				}
			} catch (AppException e) {
				throw e;
			} catch (Exception e) {
				if(LOG.isDebugEnabled()) {
					LOG.error("Can't handle API specification with class: " + clazz.getName(), e);
				}
			}
		}
		if(!failOnError) {
			LOG.error("API: '"+apiName+"' has a unkown/invalid API-Specification: '" + getContentStart(apiSpecificationContent) + "'");
			return new UnknownAPISpecification(apiName);
		}
		LOG.error("API: '"+apiName+"' has a unkown/invalid API-Specification: '" + getContentStart(apiSpecificationContent) + "'");
		throw new AppException("Can't handle API specification. No suiteable API-Specification implementation available.", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
	}
	
	static String getContentStart(byte[] apiSpecificationContent) {
		try {
			if(apiSpecificationContent == null) return "API-Specificaion is null";
			return (apiSpecificationContent.length<200) ? new String(apiSpecificationContent, 0, apiSpecificationContent.length) : new String(apiSpecificationContent, 0, 200) + "...";
		} catch (Exception e) {
			return "Cannot get content from API-Specification. " + e.getMessage();
		}		
	}
	
	private static byte[] getAPIDefinitionContent(String apiDefinitionFile, String configBaseDir) throws AppException {
		try(InputStream stream = getAPIDefinitionAsStream(apiDefinitionFile, configBaseDir)) {
			Reader reader = new InputStreamReader(stream,StandardCharsets.UTF_8);
			return IOUtils.toByteArray(reader,StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AppException("Can't read API-Definition from file", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		}
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 * @throws AppException when the import Swagger-File can't be read.
	 * @return The import Swagger-File as an InputStream
	 */
	private static InputStream getAPIDefinitionAsStream(String apiDefinitionFile, String configBaseDir) throws AppException {
		InputStream is = null;
		if(apiDefinitionFile.endsWith(".url")) {
			return getAPIDefinitionFromURL(Utils.getAPIDefinitionUriFromFile(apiDefinitionFile));
		} else if(Utils.isHttpUri(apiDefinitionFile)) {
			return getAPIDefinitionFromURL(apiDefinitionFile);
		} else {
			try {
				File inputFile = new File(apiDefinitionFile);
				if(inputFile.exists()) { 
					LOG.info("Reading API-Definition (Swagger/WSDL) from file: '" + apiDefinitionFile + "' (relative path)");
					is = new FileInputStream(apiDefinitionFile);
				} else {
					inputFile= new File(configBaseDir + File.separator + apiDefinitionFile);
					LOG.info("Reading API-Definition (Swagger/WSDL) from file: '" + inputFile.getCanonicalFile() + "' (absolute path)"); 
					if(inputFile.exists()) { 
						is = new FileInputStream(inputFile);
					} else {
						// Have to remove leading slash (Read more: https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
						is = APISpecificationFactory.class.getClassLoader().getResourceAsStream(apiDefinitionFile.replaceFirst("/", ""));
					}
				}
				if(is == null) {
					throw new AppException("Unable to read Swagger/WSDL file from: " + apiDefinitionFile, ErrorCode.CANT_READ_API_DEFINITION_FILE);
				}
			} catch (Exception e) {
				throw new AppException("Unable to read Swagger/WSDL file from: " + apiDefinitionFile, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
			}
			
		}
		return is;
	}
	
	private static InputStream getAPIDefinitionFromURL(String urlToAPIDefinition) throws AppException {
		URLParser url = new URLParser(urlToAPIDefinition);
		String uri = url.getUri();
		String username = url.getUsername();
		String password = url.getPassword();
		CloseableHttpClient httpclient = Utils.createHttpClient(uri, username, password);
		try {
			RequestConfig config = RequestConfig.custom()
					.setRelativeRedirectsAllowed(true)
					.setCircularRedirectsAllowed(true)
					.build();
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(config);
			
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity,StandardCharsets.UTF_8) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpGet, responseHandler);
            return new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new AppException("Cannot load API-Definition from URI: "+uri, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				httpclient.close();
			} catch (Exception ignore) {}
		}
	}
}
