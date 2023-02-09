package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.text.StringEscapeUtils;

public class PolicySerializer extends StdSerializer<Policy> {
	
	boolean serializeAsName = false;
	
	private static final long serialVersionUID = 1L;
	
	public PolicySerializer(boolean serializeAsName) {
		this(null);
		this.serializeAsName = serializeAsName;
	}

	public PolicySerializer(Class<Policy> policy) {
		super(policy);
	}

	@Override
	public void serialize(Policy policy, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(serializeAsName) {
			String policyName = StringEscapeUtils.unescapeHtml4(policy.getName());
			jgen.writeString(policyName);
		} else {
			jgen.writeString(policy.getId());
		}
	}
	
	@Override
	public Class<Policy> handledType() {
		return Policy.class;
	}
}
