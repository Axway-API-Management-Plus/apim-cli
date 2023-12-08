package com.axway.apim.adapter.jackson;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.APIManagerUserAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.error.AppException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserDeserializer extends StdDeserializer<User> {

    public enum Params {
        USE_LOGIN_NAME
    }

    private static final Logger LOG = LoggerFactory.getLogger(UserDeserializer.class);

    private static final long serialVersionUID = 1L;

    public UserDeserializer() {
        this(null);
    }

    public UserDeserializer(Class<User> user) {
        super(user);
    }

    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
        APIManagerUserAdapter userAdapter = APIManagerAdapter.getInstance().getUserAdapter();
        JsonNode node = jp.getCodec().readTree(jp);
        User user = null;
        // Deserialization depends on the direction
        // ConfigFile contains the LoginName / API-Manager provides the userId
        if (isUseLoginName(ctxt)) {
            // Try to initialize this user based on the loginname
            try {
                user = userAdapter.getUserForLoginName(node.asText());
            } catch (AppException e) {
                LOG.error("Error reading user with loginName: {} from API-Manager.", node.asText());
            }
            if (user != null) return user;
            // User might be null in some situations, create a new user with base init
            user = new User();
            user.setLoginName(node.asText());
        } else {
            // Try to initialize this user based on the User-ID
            try {
                user = userAdapter.getUserForId(node.asText());
            } catch (AppException e) {
                LOG.error("Error reading user with ID: {} from API-Manager.", node.asText());
            }
            if (user != null) return user;
            // This  should never happen, as the ID must be a valid API-Manager User-ID
            LOG.error("User with ID: {} not found.", node);
            user = new User();
            user.setId(node.asText());
        }
        return user;
    }

    private boolean isUseLoginName(DeserializationContext context) {
        if (context.getAttribute(Params.USE_LOGIN_NAME) == null) return false;
        return (boolean) context.getAttribute(Params.USE_LOGIN_NAME);
    }
}
