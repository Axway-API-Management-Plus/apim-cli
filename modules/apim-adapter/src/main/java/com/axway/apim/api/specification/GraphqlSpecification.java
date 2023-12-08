package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.errors.SchemaProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class GraphqlSpecification extends APISpecification {

    private static final Logger LOG = LoggerFactory.getLogger(GraphqlSpecification.class);

    @Override
    public byte[] getApiSpecificationContent() {
        return this.apiSpecificationContent;

    }

    @Override
    public void updateBasePath(String basePath, String host) {
        // Not required
    }

    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        // Not required
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        return APISpecType.GRAPHQL;
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent){
        this.apiSpecificationContent = apiSpecificationContent;
        SchemaParser schemaParser = new SchemaParser();
        try {
            schemaParser.parse(new ByteArrayInputStream(apiSpecificationContent));
            return true;
        }catch (SchemaProblem schemaProblem){
            LOG.error("Unable to parse graphql", schemaProblem);
            return false;
        }
    }
}
