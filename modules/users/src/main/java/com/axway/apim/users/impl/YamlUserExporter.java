package com.axway.apim.users.impl;

import com.axway.apim.api.model.User;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.ExportUser;
import com.axway.apim.users.lib.params.UserExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class YamlUserExporter extends JsonUserExporter{

    private static final Logger LOG = LoggerFactory.getLogger(YamlUserExporter.class);

    public YamlUserExporter(UserExportParams params, ExportResult result) {
        super(params, result);
    }
    @Override
    public void export(List<User> users) throws AppException {
        LOG.info("Exporting User in Yaml format");
        for(User user : users) {
            saveUserLocally(new ExportUser(user), this);
        }
    }
}
