package com.axway.apim.api.specification;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.HTTPClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.introspection.IntrospectionQueryBuilder;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.schema.idl.SchemaPrinter;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GraphqlIntrospectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GraphqlIntrospectionHandler.class);

    public InputStream readGraphqlSchema(HTTPClient httpClient, RequestConfig requestConfig, String url) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        IntrospectionQueryBuilder.Options options = IntrospectionQueryBuilder.Options.defaultOptions()
            .inputValueDeprecation(false)
            .isOneOf(false)
            .directiveIsRepeatable(false);
        String query = IntrospectionQueryBuilder.build(options);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("query", query);
        objectNode.put("operationName", "IntrospectionQuery");
        HttpEntity httpEntity = new StringEntity(objectMapper.writeValueAsString(objectNode), StandardCharsets.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(httpEntity);
        httpPost.setHeader("Content-Type", "application/json");
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            LOG.debug("{} {} : {} ", httpPost.getMethod(), url, statusCode);
            if(statusCode == 200) {
                IntrospectionResultToSchema introspectionResultToSchema = new IntrospectionResultToSchema();
                Map<String, Object> data = (Map<String, Object>) objectMapper.readValue(httpResponse.getEntity().getContent(), HashMap.class).get("data");
                Document document = introspectionResultToSchema.createSchemaDefinition(data);
                SchemaPrinter.Options schemaOptions = SchemaPrinter.Options.defaultOptions().includeDirectiveDefinitions(false);
                String schema = new SchemaPrinter(schemaOptions).print(document);
                return new ByteArrayInputStream(schema.getBytes(StandardCharsets.UTF_8));
            }
        }
        throw new AppException("Cannot load API-Specification", ErrorCode.CANT_READ_API_DEFINITION_FILE);
    }
}
