package com.axway.apim.actions.tasks.props;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthType;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthenticationProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuthenticationProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		List<AuthenticationProfile> authenticationProfiles = desired.getAuthenticationProfiles();
		if (!authenticationProfiles.isEmpty()) {
			desired.getAuthenticationProfiles();
			AuthenticationProfile defaultProfile = new AuthenticationProfile();
			defaultProfile.setName("_default");
			defaultProfile.setIsDefault(true);
			defaultProfile.setType(AuthType.none);
			defaultProfile.setParameters(new Properties());
			authenticationProfiles.add(defaultProfile);

			((ObjectNode) response).replace("authenticationProfiles", objectMapper.valueToTree(authenticationProfiles));

			Map<String, OutboundProfile> outboundProfiles = desired.getOutboundProfiles();

			if (outboundProfiles == null) {
				String authenticationProfileName = authenticationProfiles.get(0).getName();
				JsonNode responseOutboundProfiles = response.get("outboundProfiles").get("_default");
				
				if (responseOutboundProfiles instanceof ObjectNode) {
			        ObjectNode object = (ObjectNode) responseOutboundProfiles;
			        object.put("authenticationProfile",authenticationProfileName);
			    }

			}
		}

		return response;
	}

}
