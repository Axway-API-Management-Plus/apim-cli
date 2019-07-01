package com.axway.apim.promote.mvn.plugin;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mojo class to encapsulate stage specific configuration
 */
public class Publication {
    // stage will should be implicitly detected?
    private String stage;

    private Boolean force = false;
    private Boolean ignoreQuotas = false;
    private MODE clientOrgsMode;
    private MODE clientAppsMode;
    private Boolean ignoreAdminAccount = false;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private List<Api> apis = new ArrayList<>();

    public enum MODE {
        IGNORE,
        REPLACE,
        ADD
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("API gateway server: ").append(getHost()).append(":").append(getPort());
        sb.append("\n");
        sb.append("User: ").append(getUsername()).append(StringUtils.isNotEmpty(getPassword()) ? ":xxx" : "null");
        sb.append(getApis());

        return sb.toString();
    }

    public String getHost() {
        return host;
    }

    public Publication setHost(final String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public Publication setPort(final Integer port) {
        this.port = port;
        return this;
    }

    /**
     * User credential will  be passed to the plugin as system properties.
     * That way the plugin can be used in the pipeline and also locally.
     *
     * @return username
     */
    public String getUsername() {
        if (username == null) {
            // at this point the user was not set in the configuration.
            // we need to detect user information from the system properties
            username = System.getProperty("axway.username");
        }
        return username;
    }

    /**
     * TODO: to be defined if the user credential can defined in configuration file.
     * If yes, this method will be called during the configuration file parsing
     *
     * @param username
     *
     * @return this instance of publication
     */
    public Publication setUsername(final String username) {
        this.username = username;
        return this;
    }

    /**
     * User credential will  be passed to the plugin as system properties.
     * That way the plugin can be used in the pipeline and also locally.
     *
     * @return username
     */
    public String getPassword() {
        if (password == null) {
            // at this point the user was not set in the configuration.
            // we need to detect user information from the system properties
            password = System.getProperty("axway.password");
        }
        return password;
    }

    public Publication setPassword(final String password) {
        this.password = password;
        return this;
    }

    /**
     * @return unmodifiable list of api
     */
    public List<Api> getApis() {
        return Collections.unmodifiableList(apis);
    }

    public Publication setApis(final List<Api> apis) {
        this.apis = Collections.unmodifiableList(apis);
        return this;
    }

    public String getStage() {
        return stage;
    }

    /**
     * Extract the stage from stage configuration file
     *
     * @param stage
     *         the stage
     *
     * @return
     */
    public Publication setStage(final String stage) {
        this.stage = stage;
        return this;
    }

    public Boolean getForce() {
        return force;
    }

    public Publication setForce(final Boolean force) {
        this.force = force;
        return this;
    }

    public Boolean getIgnoreQuotas() {
        return ignoreQuotas;
    }

    public Publication setIgnoreQuotas(final Boolean ignoreQuotas) {
        this.ignoreQuotas = ignoreQuotas;
        return this;
    }

    public MODE getClientOrgsMode() {
        return clientOrgsMode;
    }

    public Publication setClientOrgsMode(final MODE clientOrgsMode) {
        this.clientOrgsMode = clientOrgsMode;
        return this;
    }

    public MODE getClientAppsMode() {
        return clientAppsMode;
    }

    public Publication setClientAppsMode(final MODE clientAppsMode) {
        this.clientAppsMode = clientAppsMode;
        return this;
    }

    public Boolean getIgnoreAdminAccount() {
        return ignoreAdminAccount;
    }

    public Publication setIgnoreAdminAccount(final Boolean ignoreAdminAccount) {
        this.ignoreAdminAccount = ignoreAdminAccount;
        return this;
    }
}
