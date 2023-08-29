package com.axway.apim.appimport.adapter;

import com.axway.apim.ClientAppAdapter;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer.DeserializeMode;
import com.axway.apim.adapter.user.APIManagerUserAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.ApplicationPermission;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientAppConfigAdapter extends ClientAppAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientAppConfigAdapter.class);

    AppImportParams importParams;

    public ClientAppConfigAdapter(AppImportParams params) {
        this.importParams = params;
        this.result = new Result();
    }

    public ClientAppConfigAdapter(AppImportParams params, Result result) {
        this.importParams = params;
        this.result = result;
    }

    @Override
    protected void readConfig() throws AppException {
        ObjectMapper mapper = new ObjectMapper();
        String config = importParams.getConfig();
        String stage = importParams.getStage();

        File configFile = Utils.locateConfigFile(config);
        if (!configFile.exists()) return;
        File stageConfig = Utils.getStageConfig(stage, importParams.getStageConfig(), configFile);
        List<ClientApplication> baseApps;
        // Try to read a list of applications
        try {
            // Check the config file is json
            mapper.readTree(configFile);
            LOG.debug("Handling JSON Configuration file: {}", configFile);
        } catch (IOException ioException) {
            mapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
            LOG.debug("Handling Yaml Configuration file: {}", configFile);
        }
        try {
            mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
            mapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(DeserializeMode.configFile)));
            baseApps = mapper.readValue(Utils.substituteVariables(configFile), new TypeReference<List<ClientApplication>>() {
            });
            if (stageConfig != null) {
                throw new AppException("Stage overrides are not supported for application lists.", ErrorCode.CANT_READ_CONFIG_FILE);
            } else {
                this.apps = baseApps;
            }
            // Try to read single application
        } catch (MismatchedInputException me) {
            try {
                LOG.debug("Error reading array of applications, hence trying to read single application now.");
                ClientApplication app = mapper.readValue(Utils.substituteVariables(configFile), ClientApplication.class);
                if (stageConfig != null) {
                    try {
                        ObjectReader updater = mapper.readerForUpdating(app);
                        app = updater.readValue(Utils.substituteVariables(stageConfig));
                    } catch (FileNotFoundException e) {
                        LOG.warn("No config file found for stage: {}", stage);
                    }
                }
                this.apps = new ArrayList<>();
                this.apps.add(app);
            } catch (Exception pe) {
                throw new AppException("Cannot read application(s) from config file: " + config, "Exception: " + pe.getClass().getName() + ": " + pe.getMessage(), ErrorCode.ERR_CREATING_APPLICATION, pe);
            }
        } catch (Exception e) {
            throw new AppException("Cannot read application(s) from config file: " + config, "Exception: " + e.getClass().getName() + ": " + e.getMessage(), ErrorCode.ERR_CREATING_APPLICATION, e);
        }
        try {
            addImage(apps, configFile.getCanonicalFile().getParentFile());
            addOAuthCertificate(apps, configFile.getCanonicalFile().getParentFile());
        } catch (Exception e) {
            throw new AppException("Cannot read image/certificate for application(s) from config file: " + config, ErrorCode.ERR_CREATING_APPLICATION, e);
        }
        addAPIAccess(apps, result);
        validateCustomProperties(apps);
        validateAppPermissions(apps);
    }

    private void addImage(List<ClientApplication> apps, File parentFolder) throws AppException {
        for (ClientApplication app : apps) {
            String imageUrl = app.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) continue;
            if (imageUrl.startsWith("data:")) {
                app.setImage(Image.createImageFromBase64(imageUrl));
            } else {
                app.setImage(Image.createImageFromFile(new File(parentFolder + File.separator + imageUrl)));
            }
        }
    }

    private void addOAuthCertificate(List<ClientApplication> apps, File parentFolder) throws AppException {
        for (ClientApplication app : apps) {
            for (ClientAppCredential cred : app.getCredentials()) {
                if (cred instanceof OAuth && ((OAuth) cred).getCert() != null) {
                    String certificate = ((OAuth) cred).getCert();
                    if (certificate.startsWith("data:")) {
                        byte[] data = Base64.getDecoder().decode(certificate.replaceFirst("data:.+,", ""));
                        ((OAuth) cred).setCert(new String(data));
                    } else {
                        File certFile = new File(parentFolder + File.separator + certificate);
                        if (!certFile.exists()) {
                            throw new AppException("Certificate file: '" + certFile + "' not found.", ErrorCode.UNXPECTED_ERROR);
                        }
                        try {
                            String certBlob = new String(Files.readAllBytes(certFile.toPath()));
                            ((OAuth) cred).setCert(certBlob);
                        } catch (Exception e) {
                            throw new AppException("Can't read certificate from disc", ErrorCode.UNXPECTED_ERROR, e);
                        }
                    }
                }
            }
        }
    }

    private void addAPIAccess(List<ClientApplication> apps, Result result) throws AppException {
        APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
        for (ClientApplication app : apps) {
            if (app.getApiAccess() == null) continue;
            Iterator<APIAccess> it = app.getApiAccess().iterator();
            while (it.hasNext()) {
                APIAccess apiAccess = it.next();
                List<API> apis = apiAdapter.getAPIs(new APIFilter.Builder()
                        .hasName(apiAccess.getApiName())
                        .build()
                    , false);
                if (apis == null || apis.isEmpty()) {
                    LOG.error("API with name: {} not found. Ignoring this APIs.", apiAccess.getApiName());
                    result.setError(ErrorCode.UNKNOWN_API);
                    it.remove();
                    continue;
                }
                if (apis.size() > 1 && apiAccess.getApiVersion() == null) {
                    LOG.error("Found: {} APIs with name: {} not providing a version. Ignoring this APIs.", apis.size(), apiAccess.getApiName());
                    result.setError(ErrorCode.UNKNOWN_API);
                    it.remove();
                    continue;
                }
                API api = apis.get(0);
                apiAccess.setApiId(api.getId());
            }
        }
    }

    private void validateCustomProperties(List<ClientApplication> apps) throws AppException {
        for (ClientApplication app : apps) {
            Utils.validateCustomProperties(app.getCustomProperties(), Type.application);
        }
    }

    private void validateAppPermissions(List<ClientApplication> apps) throws AppException {
        APIManagerUserAdapter userAdapter = APIManagerAdapter.getInstance().userAdapter;
        for (ClientApplication app : apps) {
            if (app.getPermissions() == null || app.getPermissions().isEmpty()) continue;
            // First check, if there is an ALL User
            for (ApplicationPermission permission : app.getPermissions()) {
                if ("ALL".equals(permission.getUsername())) {
                    // Create a map of all usernames
                    Map<String, ApplicationPermission> usernames = app.getPermissions().stream().collect(
                        Collectors.toMap(ApplicationPermission::getUsername, Function.identity()));
                    // Get all users for the app organization
                    List<User> allOrgUsers = userAdapter.getUsers(new UserFilter.Builder().hasOrganization(app.getOrganization().getName()).build());
                    for (User user : allOrgUsers) {
                        // Only add permission based on ALL if not manually configured
                        if (!usernames.containsKey(user.getLoginName())) {
                            ApplicationPermission appPerm = new ApplicationPermission();
                            appPerm.setUser(user);
                            appPerm.setPermission(permission.getPermission());
                            app.getPermissions().add(appPerm);
                        }
                    }
                    break;
                }
            }
            app.getPermissions().removeIf(e -> e.getUsername().equals("ALL"));
            // Check individual permissions (e.g. single usernames)
            for (int i = 0; i < app.getPermissions().size(); i++) {
                ApplicationPermission permission = app.getPermissions().get(i);
                if (permission.getUser() != null) continue;
                User user = userAdapter.getUserForLoginName(permission.getUsername());
                if (user == null) {
                    LOG.warn("Cannot share application with user: {} as user does not exists.", permission.getUsername());
                    app.getPermissions().remove(i);
                    continue;
                }
                permission.setUser(user);
            }
        }
    }
}
