package com.axway.apim.adapter.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkdownLocalDeserializer extends StdDeserializer<List<String>> {

	private static final long serialVersionUID = 1L;

	public MarkdownLocalDeserializer() {
		this(null);
	}

	public MarkdownLocalDeserializer(Class<List<String>> user) {
		super(user);
	}



	@Override
	public List<String> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		List<String> markdownLocal = new ArrayList<>();
		if(node instanceof TextNode) {
			markdownLocal.add(node.asText());
		} else if(node instanceof ArrayNode) {
			for(JsonNode items : node) {
				markdownLocal.add(items.asText());
			}
		} else {
			return Collections.emptyList();
		}
		return markdownLocal;
	}
}
