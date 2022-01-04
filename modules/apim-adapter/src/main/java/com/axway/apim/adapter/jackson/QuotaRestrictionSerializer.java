package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.QuotaRestriction;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class QuotaRestrictionSerializer extends StdSerializer<QuotaRestriction> {
	
	private static final long serialVersionUID = 1L;

	public QuotaRestrictionSerializer() {
		this(null);
	}
	
	public QuotaRestrictionSerializer(Class<QuotaRestriction> t) {
		super(t);
	}

	@Override
	public void serialize(QuotaRestriction quotaRestriction, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		if(quotaRestriction.getRestrictedAPI()==null) {
			jgen.writeObjectField("api", "*");
			jgen.writeObjectField("method", "*");
		} else {
			jgen.writeObjectField("api", quotaRestriction.getRestrictedAPI().getName());
			jgen.writeObjectField("apiPath", quotaRestriction.getRestrictedAPI().getPath());
			jgen.writeObjectField("method", "*");
			if(quotaRestriction.getRestrictedAPI().getVhost()!=null) {
				jgen.writeObjectField("vhost", quotaRestriction.getRestrictedAPI().getVhost());
			}
			if(quotaRestriction.getRestrictedAPI().getApiRoutingKey()!=null) {
				jgen.writeObjectField("apiRoutingKey", quotaRestriction.getRestrictedAPI().getApiRoutingKey());
			}
		}
		jgen.writePOJOField("config",quotaRestriction.getConfig());
		jgen.writeEndObject();
	}
	
	@Override
	public Class<QuotaRestriction> handledType() {
		return QuotaRestriction.class;
	}
}
