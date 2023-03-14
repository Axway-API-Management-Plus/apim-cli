package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

public class WSDLSpecification extends APISpecification {

    private final Logger LOG = LoggerFactory.getLogger(WSDLSpecification.class);

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        return APISpecType.WSDL_API;
    }

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
        // For SOAP services, the WSDL has not been adapted in any case,
        // so the necessary service profile is to be created here.
        if (backendBasePath != null) {
            ServiceProfile serviceProfile = new ServiceProfile();
            serviceProfile.setBasePath(backendBasePath);
            if (api.getServiceProfiles() == null) {
                api.setServiceProfiles(new LinkedHashMap<>());
            }
            api.getServiceProfiles().put("_default", serviceProfile);
        }
    }


    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        super.parse(apiSpecificationContent);
        if (apiSpecificationFile.toLowerCase().endsWith(".url")) {
            apiSpecificationFile = Utils.getAPIDefinitionUriFromFile(apiSpecificationFile);
        }
        if (apiSpecificationFile.toLowerCase().endsWith("?wsdl") ||
            apiSpecificationFile.toLowerCase().endsWith(".wsdl") ||
            apiSpecificationFile.toLowerCase().endsWith("?singlewsdl")) {
            return true;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // to be compliant, completely disable DOCTYPE declaration:
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // or completely disable external entities declarations:
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // or prohibit the use of all protocols by external entities:
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            // or disable entity expansion but keep in mind that this doesn't prevent fetching external entities
            // and this solution is not correct for OpenJDK < 13 due to a bug: https://bugs.openjdk.java.net/browse/JDK-8206132
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(apiSpecificationContent));
            String wsdlNamespace = document.getDocumentElement().getNamespaceURI();
            if (wsdlNamespace != null && wsdlNamespace.contains("http://schemas.xmlsoap.org/wsdl"))
                return true;
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error("Error parsing WSDL : {}", e.getMessage());
            return false;
        }
        LOG.debug("Not a WSDL specification..");
        return false;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
