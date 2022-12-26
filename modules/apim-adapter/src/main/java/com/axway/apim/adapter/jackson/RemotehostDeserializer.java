package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class RemotehostDeserializer extends StdDeserializer<RemoteHost> {
	
	public enum Params {
		validateRemoteHost
	}
	
	static Logger LOG = LoggerFactory.getLogger(RemotehostDeserializer.class);
	
	private static final long serialVersionUID = 1L;
	
	public RemotehostDeserializer() {
		this(null);
	}

	public RemotehostDeserializer(Class<RemoteHost> remoteHost) {
		super(remoteHost);
	}

	@Override
	public RemoteHost deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String remoteHostName;
		int remoteHostPort;
		// This must have the format my.host.com:7889
		String givenRemoteHost = node.asText();
		// 
		if(!givenRemoteHost.contains(":")) {
			remoteHostName = givenRemoteHost;
			remoteHostPort = 443;
		} else {
			String[] given = givenRemoteHost.split(":");
			remoteHostName = given[0];
			remoteHostPort = Integer.parseInt(given[1]);
		}
		RemoteHost remoteHost = APIManagerAdapter.getInstance().remoteHostsAdapter.getRemoteHost(remoteHostName, remoteHostPort);
		if(remoteHost==null) {
			if(validateRemoteHost(ctxt)) {
				throw new AppException("The given remote host: '"+remoteHostName+":"+remoteHostPort+"' is unknown.", ErrorCode.UNKNOWN_REMOTE_HOST);
			} else {
				LOG.warn("The given remote host: {}:{} is unknown.",remoteHostName,remoteHostPort);
			}
		}
		return remoteHost;
	}
	
	private Boolean validateRemoteHost(DeserializationContext ctxt) {
		if(ctxt.getAttribute(Params.validateRemoteHost)==null) return true;
		return (Boolean)ctxt.getAttribute(Params.validateRemoteHost);
	}
}
