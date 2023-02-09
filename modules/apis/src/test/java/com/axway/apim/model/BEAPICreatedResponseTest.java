package com.axway.apim.model;

import com.axway.apim.api.API;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class BEAPICreatedResponseTest {
	
	private static final String testPackage = "com/axway/apim/model/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void parseResponseHavingCreatedOn() throws IOException {
		JsonNode jsonNode = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream(testPackage + "BEAPICreatedWithCreatedOn.json"));
		API createdAPI = new API();
		createdAPI.setApiId(jsonNode.get("id").asText());
		createdAPI.setName(jsonNode.get("name").asText());
		createdAPI.setName(jsonNode.get("description").asText());
		
		createdAPI.setCreatedOn(Long.parseLong(jsonNode.get("createdOn").asText()));
		
		Assert.assertEquals(1605099396581L, createdAPI.getCreatedOn().longValue());
		// Tests if the Unicode-Description is parsed without giving a encoding
		Assert.assertEquals("提供銀行身份認證整合 Axway APIM OAuth Authorization Code Grant Flow\n", jsonNode.get("description").asText());
	}
}
