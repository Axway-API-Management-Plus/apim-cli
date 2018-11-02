package com.axway.apim.actions.tasks;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public class ImportBackendAPI extends AbstractAPIMTask implements IResponseParser {

	public static RestAPICall execute(IAPIDefinition desired, IAPIDefinition actual) {
		LOG.info("Importing Swagger Definition");
		URI uri;
		HttpEntity entity;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/apirepo/import/")
					.setParameter("field", "name").setParameter("op", "eq").setParameter("value", "API Development").build();
			
			entity = MultipartEntityBuilder.create()
					.addTextBody("name", desired.getApiName())
					.addTextBody("type", "swagger")
					.addBinaryBody("file", ((APIImportDefinition)desired).getSwaggerAsStream(), ContentType.create("application/octet-stream"), "filename")
					.addTextBody("fileName", "XYZ").addTextBody("organizationId", desired.getOrgId())
					.addTextBody("integral", "false").addTextBody("uploadType", "html5").build();
			RestAPICall importSwagger = new POSTRequest(entity, uri);
			importSwagger.registerResponseCallback(new ImportBackendAPI());
			importSwagger.setContentType(null);
			return importSwagger;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public JsonNode parseResponse(InputStream response) {
		String backendAPIId = JsonPath.parse(response).read("$.id", String.class);
		Transaction.getInstance().put("backendAPIId", backendAPIId);
		return null;
	}

}
