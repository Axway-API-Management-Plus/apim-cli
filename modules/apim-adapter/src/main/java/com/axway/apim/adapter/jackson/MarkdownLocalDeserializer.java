package com.axway.apim.adapter.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class MarkdownLocalDeserializer extends StdDeserializer<List<String>> {
	
	public static enum Params {
		useLoginName
	}
	
	static Logger LOG = LoggerFactory.getLogger(MarkdownLocalDeserializer.class);
	
	private static final long serialVersionUID = 1L;
	
	public MarkdownLocalDeserializer() {
		this(null);
	}

	public MarkdownLocalDeserializer(Class<List<String>> user) {
		super(user);
	}
	
	

	@Override
	public List<String> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		List<String> markdownLocal = new ArrayList<String>();
		if(node instanceof TextNode) {
			markdownLocal.add(node.asText());
		} else if(node instanceof ArrayNode) {
			for(JsonNode items : (ArrayNode)node) {
				markdownLocal.add(items.asText());
			}
		} else {
			return null;
		}
		return markdownLocal;
	}
}
