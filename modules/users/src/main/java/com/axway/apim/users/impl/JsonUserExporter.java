package com.axway.apim.users.impl;

import com.axway.apim.adapter.jackson.ImageSerializer;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.lib.ExportUser;
import com.axway.apim.users.lib.params.UserExportParams;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class JsonUserExporter extends UserResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUserExporter.class);

    public JsonUserExporter(UserExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<User> users) throws AppException {
        for (User user : users) {
            saveUserLocally(new ObjectMapper(), new ExportUser(user), "/user-config.json");
        }
    }

    public void saveUserLocally(ObjectMapper mapper, ExportUser user, String configFile) throws AppException {
        File localFolder = null;
        if (!EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            String folderName = getExportFolder(user);
            String targetFolder = params.getTarget();
            localFolder = new File(targetFolder + File.separator + folderName);
            LOG.info("Going to export users into folder: {}", localFolder);
            if (localFolder.exists()) {
                if (UserExportParams.getInstance().isDeleteTarget()) {
                    Utils.deleteDirectory(localFolder);
                } else {
                    LOG.warn("Local export folder: {} already exists. User will not be exported. (You may set -deleteTarget)", localFolder);
                    this.hasError = true;
                    return;
                }
            }
            if (!localFolder.mkdirs()) {
                throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
            }
        }
        mapper.registerModule(new SimpleModule().addSerializer(Image.class, new ImageSerializer()));
        FilterProvider filters = new SimpleFilterProvider()
            .addFilter("UserFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("id", "dn"))
            .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept());
        mapper.setFilterProvider(filters);
        mapper.setSerializationInclusion(Include.NON_NULL);
        writeContent(mapper, user, configFile, localFolder);
        if (user.getImage() != null && !EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            writeBytesToFile(user.getImage().getImageContent(), localFolder + File.separator + user.getImage().getBaseFilename());
        }
        LOG.info("Successfully exported user into folder: {}", localFolder);
    }

    public void writeContent(ObjectMapper mapper, ExportUser user, String configFile, File localFolder) throws AppException {
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                mapper.writeValue(System.out, user);
            } else {
                mapper.writeValue(new File(localFolder.getCanonicalPath() + configFile), user);
            }
            this.result.addExportedFile((localFolder != null ? localFolder.getCanonicalPath() : null) + configFile);
        } catch (Exception e) {
            throw new AppException("Can't write configuration file for user: '" + user.getName() + "'", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    private String getExportFolder(ExportUser user) {
        String loginName = user.getLoginName();
        loginName = loginName.replace(" ", "-");
        return loginName;
    }

    @Override
    public UserFilter getFilter() {
        return getBaseFilterBuilder().includeImage(true).build();
    }

    public static void writeBytesToFile(byte[] bFile, String fileDest) throws AppException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileDest)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
