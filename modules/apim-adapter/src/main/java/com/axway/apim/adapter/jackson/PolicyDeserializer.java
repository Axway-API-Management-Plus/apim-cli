package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class PolicyDeserializer extends StdDeserializer<Policy> {
	
	private static final long serialVersionUID = 1L;
	
	public PolicyDeserializer() {
		this(null);
	}

	public PolicyDeserializer(Class<Policy> policy) {
		super(policy);
	}

	@Override
	public Policy deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String policy = node.asText();
		if(StringUtils.isEmpty(policy)) return null;
		if(policy.startsWith("<key")) {
			String policyName = getName(policy);
			Policy createdColicy = new Policy(policyName);
			createdColicy.setId(policy);
			return createdColicy;
		} else {
			try {
				return APIManagerAdapter.getInstance().policiesAdapter.getPolicyForName(PolicyType.getTypeForJsonKey(jp.currentName()), policy);
			} catch (AppException e) {
				throw new IOException(e);
			}
		}
	}
	
	private static String getName(String policy) {
		if(policy.startsWith("<key")) {
			policy = policy.substring(policy.indexOf("<key type='FilterCircuit'>"));
			policy = policy.substring(policy.indexOf("value='")+7, policy.lastIndexOf("'/></key>"));
		}
		return policy;
	}
}
