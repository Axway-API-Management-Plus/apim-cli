package com.axway.apim.apiimport.actions.tasks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImportBackendAPI extends AbstractAPIMTask implements IResponseParser {

	public ImportBackendAPI(IAPI desiredState, IAPI actualState) {
		super(desiredState, actualState);
	}

	public void execute() throws AppException {
		LOG.info("Importing backend API (Swagger/WSDL Import)");
		try {
			if(desiredState.getAPIDefinition().getAPIDefinitionType()==IAPI.WSDL_API) {
				importFromWSDL();
			} else {
				importFromSwagger();
			}
		} catch (Exception e) {
			throw new AppException("Can't import definition / Create BE-API.", ErrorCode.CANT_CREATE_BE_API, e);
		}
	}

	private void importFromWSDL() throws URISyntaxException, AppException, IOException {
		URI uri;
		HttpEntity entity = new StringEntity("");
		String username=null;
		String pass=null;
		String wsdlUrl=null;
		String completeWsdlUrl=null;
		if(this.desiredState.getAPIDefinition().getApiSpecificationFile().endsWith(".url")) {
			completeWsdlUrl = Utils.getAPIDefinitionUriFromFile(this.desiredState.getAPIDefinition().getApiSpecificationFile());
		} else {
			completeWsdlUrl = this.desiredState.getAPIDefinition().getApiSpecificationFile();
		}
		wsdlUrl = extractURI(completeWsdlUrl);
		username=extractUsername(completeWsdlUrl);
		pass=extractPassword(completeWsdlUrl);

		
		URIBuilder uriBuilder = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/apirepo/importFromUrl/")
				.setParameter("organizationId", this.desiredState.getOrganizationId())
				.setParameter("type", "wsdl")
				.setParameter("url", wsdlUrl)
				.setParameter("name", this.desiredState.getName());
		if (username!=null) {
			uriBuilder.setParameter("username", username);
			uriBuilder.setParameter("password", pass);
		}
		uri=uriBuilder.build();
		RestAPICall importWSDL = new POSTRequest(entity, uri, this);
		importWSDL.setContentType("application/x-www-form-urlencoded");
		HttpResponse httpResponse = importWSDL.execute();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if(statusCode != 201){
			LOG.error("Received Status-Code: " +statusCode+ ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
			throw new AppException("Can't import WSDL from URL / Create BE-API.", ErrorCode.CANT_CREATE_BE_API);
		}
	}

	private void importFromSwagger() throws URISyntaxException, AppException, IOException {
		URI uri;
		HttpEntity entity;
		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2")) {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/apirepo/import/").build();
		} else {
			// Not sure, if 7.5.3 still needs it that way!
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/apirepo/import/")
					.setParameter("field", "name").setParameter("op", "eq").setParameter("value", "API Development").build();
		}
		
		entity = MultipartEntityBuilder.create()
				.addTextBody("name", this.desiredState.getName())
				.addTextBody("type", "swagger")
				.addBinaryBody("file", ((DesiredAPI)this.desiredState).getAPIDefinition().getApiSpecificationContent(), ContentType.create("application/json"), "filename")
				.addTextBody("fileName", "XYZ").addTextBody("organizationId", this.desiredState.getOrganizationId())
				.addTextBody("integral", "false").addTextBody("uploadType", "html5").build();
		RestAPICall importSwagger = new POSTRequest(entity, uri, this);
		importSwagger.setContentType(null);
		HttpResponse httpResponse = importSwagger.execute();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if(statusCode != 201){
			LOG.error("Received Status-Code: " +statusCode+ ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
			throw new AppException("Can't import Swagger-definition / Create BE-API.", ErrorCode.CANT_CREATE_BE_API);
		}
	}
	
	private String extractUsername(String url) {
		String[] temp = url.split("@");
		if(temp.length==2) {
			return temp[0].substring(0, temp[0].indexOf("/"));
		}
		return null;
	}
	
	private String extractPassword(String url) {
		String[] temp = url.split("@");
		if(temp.length==2) {
			return temp[0].substring(temp[0].indexOf("/")+1);
		}
		return null;
	}
	
	private String extractURI(String url) throws AppException
	{
		String[] temp = url.split("@");
		if(temp.length==1) {
			return temp[0];
		} else if(temp.length==2) {
			return temp[1];
		} else {
			throw new AppException("WSDL-URL has an invalid format. ", ErrorCode.CANT_READ_WSDL_FILE);
		}
	}
	
	
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		String response = null;
		try {
			if(httpResponse.getStatusLine().getStatusCode()!=201) {
				Object lastRequest = Transaction.getInstance().get("lastRequest");
				ErrorState.getInstance().setError("Error importing BE-API. "
						+ "Unexpected response from API-Manager: " + httpResponse.getStatusLine() + " " + EntityUtils.toString(httpResponse.getEntity()) + ". "
								+ "Last request: '"+lastRequest+"'. "
								+ "Please check the API-Manager traces.", ErrorCode.CANT_CREATE_API_PROXY, false);
				throw new AppException("Error creating API-Proxy", ErrorCode.CANT_CREATE_API_PROXY);
			}
			response = EntityUtils.toString(httpResponse.getEntity());
			JsonNode jsonNode = objectMapper.readTree(response);
			String backendAPIId = jsonNode.findPath("id").asText();
			Transaction.getInstance().put("backendAPIId", backendAPIId);
			// The createdOnInformation is required for the rollback action to identify the created BE-API.
			Transaction.getInstance().put("backendAPICreatedOn", jsonNode.findPath("createdOn").asText());
			return null;
		} catch (IOException e) {
			throw new AppException("Cannot parse JSON-Payload after create BE-API.", ErrorCode.CANT_CREATE_BE_API, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
}
