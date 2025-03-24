package com.axway.apim.api.specification;

import com.axway.apim.lib.error.AppException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAS31xSpecification extends OASSpecification {
    private static final Logger LOG = LoggerFactory.getLogger(OAS31xSpecification.class);
    public static final String OPENAPI = "openapi";

    public OAS31xSpecification() {
        super();
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        if (mapper.getFactory() instanceof YAMLFactory) {
            return APISpecType.OPEN_API_31_YAML;
        }
        return APISpecType.OPEN_API_31;
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) {
        try {
            this.apiSpecificationContent = apiSpecificationContent;
            setMapperForDataFormat();
            if (this.mapper == null) return false;
            openApiNode = this.mapper.readTree(apiSpecificationContent);
            LOG.debug("openapi tag value : {}", openApiNode.get(OPENAPI));
            return openApiNode.has(OPENAPI) && openApiNode.get(OPENAPI).asText().startsWith("3.1.");
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("No OpenAPI 3.1 specification.", e);
            }
            return false;
        }
    }
}
