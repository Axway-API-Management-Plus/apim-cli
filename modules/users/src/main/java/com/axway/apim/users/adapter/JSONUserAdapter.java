package com.axway.apim.users.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.lib.UserImportParams;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONUserAdapter extends UserAdapter {

    private final ObjectMapper mapper = new ObjectMapper();

    public JSONUserAdapter(UserImportParams params) {
        super(params);
    }

    public void readConfig() throws AppException {
        String config = importParams.getConfig();
        String stage = importParams.getStage();

        File configFile = Utils.locateConfigFile(config);
        if (!configFile.exists()) return;
        File stageConfig = Utils.getStageConfig(stage, importParams.getStageConfig(), configFile);
        List<User> baseUsers;
        // Try to read a list of users
        try {
            baseUsers = mapper.readValue(Utils.substituteVariables(configFile), new TypeReference<List<User>>() {
            });
            if (stageConfig != null) {
                throw new AppException("Stage overrides are not supported for users lists.", ErrorCode.CANT_READ_CONFIG_FILE);
            } else {
                this.users = baseUsers;
            }
            // Try to read single user
        } catch (MismatchedInputException me) {
            try {
                User user = mapper.readValue(Utils.substituteVariables(configFile), User.class);
                if (stageConfig != null) {
                    try {
                        ObjectReader updater = mapper.readerForUpdating(user);
                        user = updater.readValue(Utils.substituteVariables(stageConfig));
                    } catch (FileNotFoundException e) {
                        LOG.warn("No config file found for stage: {}", stage);
                    }
                }
                this.users = new ArrayList<>();
                this.users.add(user);
            } catch (Exception pe) {
                pe.printStackTrace();
                throw new AppException("Cannot read user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, pe);
            }
        } catch (Exception e) {
            throw new AppException("Cannot read user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, e);
        }
        try {
            addImage(users, configFile.getCanonicalFile().getParentFile());
        } catch (Exception e) {
            throw new AppException("Cannot read image for user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, e);
        }
        validateCustomProperties(users);
        setInternalUser(users);
    }

    private void addImage(List<User> users, File parentFolder) throws AppException {
        for (User user : users) {
            if (user.getImageUrl() == null || user.getImageUrl().equals("")) continue;
            user.setImage(Image.createImageFromFile(new File(parentFolder + File.separator + user.getImageUrl())));
        }
    }

    private void validateCustomProperties(List<User> users) throws AppException {
        for (User user : users) {
            Utils.validateCustomProperties(user.getCustomProperties(), Type.user);
        }
    }

    private void setInternalUser(List<User> users) {
        for (User user : users) {
            user.setType("internal"); // Default to internal, as external makes no sense using the CLI
        }
    }
}
