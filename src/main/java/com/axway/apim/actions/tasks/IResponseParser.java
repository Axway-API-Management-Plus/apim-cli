package com.axway.apim.actions.tasks;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;

public interface IResponseParser {
	public JsonNode parseResponse(InputStream response);
}
