package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.text.StringEscapeUtils;

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
			String policyName =  StringEscapeUtils.unescapeHtml4(getName(policy));
			Policy createdPolicy = new Policy(policyName);
			createdPolicy.setId(policy);
			return createdPolicy;
		} else {
			return APIManagerAdapter.getInstance().getPoliciesAdapter().getPolicyForName(PolicyType.getTypeForJsonKey(jp.currentName()), policy);
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
