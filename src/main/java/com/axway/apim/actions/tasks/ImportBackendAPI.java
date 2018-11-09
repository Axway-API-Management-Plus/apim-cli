package com.axway.apim.actions.tasks;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

	public ImportBackendAPI(IAPIDefinition desiredState, IAPIDefinition actualState) {
		super(desiredState, actualState);
		// TODO Auto-generated constructor stub
	}

	public void execute() {
		LOG.info("Importing Swagger Definition");
		URI uri;
		HttpEntity entity;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/apirepo/import/")
					.setParameter("field", "name").setParameter("op", "eq").setParameter("value", "API Development").build();
			
			entity = MultipartEntityBuilder.create()
					.addTextBody("name", this.desiredState.getApiName())
					.addTextBody("type", "swagger")
					.addBinaryBody("file", ((APIImportDefinition)this.desiredState).getSwaggerAsStream(), ContentType.create("application/octet-stream"), "filename")
					.addTextBody("fileName", "XYZ").addTextBody("organizationId", this.desiredState.getOrgId())
					.addTextBody("integral", "false").addTextBody("uploadType", "html5").build();
			RestAPICall importSwagger = new POSTRequest(entity, uri, this);
			importSwagger.setContentType(null);
			importSwagger.execute();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public JsonNode parseResponse(HttpResponse response) {
		InputStream json = getJSONPayload(response);
		String backendAPIId = JsonPath.parse(json).read("$.id", String.class);
		Transaction.getInstance().put("backendAPIId", backendAPIId);
		return null;
	}

}
