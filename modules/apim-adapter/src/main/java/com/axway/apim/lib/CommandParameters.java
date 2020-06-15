package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.TestIndicator;

public class CommandParameters {
	
	private static Logger LOG = LoggerFactory.getLogger(CommandParameters.class);
	
	public static String MODE_REPLACE	= "replace";
	public static String MODE_IGNORE	= "ignore";
	public static String MODE_ADD		= "add";
	
	public static String APIM_CLI_HOME = "AXWAY_APIM_CLI_HOME";
	
	private static CommandParameters instance;
	
	private List<CacheType> cachesToClear = null;
	
	int port = 8075;
	
	private CommandLine cmd;
	
	private CommandLine internalCmd;
	
	private EnvironmentProperties envProperties;
	
	private Map<String, String> manualParams;
	
	/**
	 * Use this constructor manually build a CommandParameters instance. 
	 * This is useful when calling Swagger-Promote other classes or running tests.
	 * @param manualParams to be used for parameter lookup internally
	 */
	public CommandParameters (Map<String, String> manualParams) {
		this.manualParams = manualParams;
		CommandParameters.instance = this;
	}
	
	public CommandParameters (CommandLine cmd) throws AppException {
		this(cmd, null, null);
	}
	
	public CommandParameters (CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment) throws AppException {
		this(cmd, internalCmd, environment, true);
	}
	
	public CommandParameters (CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment, boolean validateParams) throws AppException {
		this.cmd = cmd;
		this.internalCmd = internalCmd;
		this.envProperties = environment;
		if(validateParams) validateRequiredParameters();
		CommandParameters.instance = this;
	}
	
	public static synchronized CommandParameters getInstance() {
		if(CommandParameters.instance == null && TestIndicator.getInstance().isTestRunning()) {
			return new CommandParameters(new HashMap<>()); // Skip this, just return an empty CommandParams to avoid NPE
		}
		if (CommandParameters.instance == null) {
			LOG.error("CommandParameters has not been initialized.");
			throw new RuntimeException("CommandParameters has not been initialized.");
		}
		return CommandParameters.instance;
	}

	public String getUsername() {
		if(getValue("username")!=null) {
			return getValue("username");
		} else {
			// Perhaps the admin_username is given
			return getValue("admin_username");
		}
	}

	public String getPassword() {
		if(getValue("password")!=null) {
			return getValue("password");
		} else {
			// Perhaps the admin_password is given (hopefully in combination with the admin_username)
			return getValue("admin_password");
		}
	}
	
	public String getAdminUsername() {
		return getValue("admin_username");
	}

	public String getAdminPassword() {
		return getValue("admin_password");
	}

	public String getHostname() {
		return getValue("host");
	}

	public int getPort() {
		if(getValue("port")==null) return port;
		return Integer.parseInt(getValue("port"));
	}

	public boolean isForce() {
		if(hasOption("force")) return true;
		if(getValue("force")==null) return false;
		return Boolean.parseBoolean(getValue("force"));
	}
	
	public boolean isIgnoreQuotas() {
		if(hasOption("ignoreQuotas")) return true;
		if(getValue("ignoreQuotas")==null) return false;
		return Boolean.parseBoolean(getValue("ignoreQuotas"));
	}
	
	public boolean isIgnoreClientApps() {
		if(getClientAppsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getQuotaMode() {
		if(getValue("quotaMode")==null) return MODE_ADD;
		return getValue("quotaMode").toLowerCase();
	}
	
	public String getClientAppsMode() {
		if(getValue("clientAppsMode")==null) return MODE_ADD;
		return getValue("clientAppsMode").toLowerCase();
	}
	
	public boolean isIgnoreClientOrgs() {
		if(getClientOrgsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getClientOrgsMode() {
		if(getValue("clientOrgsMode")==null) return MODE_ADD;
		return getValue("clientOrgsMode").toLowerCase();
	}
	
	public String getAPIManagerURL() {
		return "https://"+this.getHostname()+":"+this.getPort();
	}
	
	public boolean ignoreAdminAccount() {
		if(hasOption("ignoreAdminAccount")) return true;
		if(getValue("ignoreAdminAccount")==null) return false;
		return Boolean.parseBoolean(getValue("ignoreAdminAccount"));
	}
	
	public boolean allowOrgAdminsToPublish() {
		if(getValue("allowOrgAdminsToPublish")==null) return true;
		return Boolean.parseBoolean(getValue("allowOrgAdminsToPublish"));
	}
	
	public String getDetailsExportFile() {
		if(getValue("detailsExportFile")==null) return null;
		return getValue("detailsExportFile");
	}
	
	public boolean replaceHostInSwagger() {
		if(getValue("replaceHostInSwagger")==null) return true;
		return Boolean.parseBoolean(getValue("replaceHostInSwagger"));
	}
	
	public boolean rollback() {
		if(getValue("rollback")==null) return true;
		return Boolean.parseBoolean(getValue("rollback"));
	}
	
	public boolean changeOrganization() {
		if(hasOption("changeOrganization")) return true;
		if(getValue("changeOrganization")==null) return false;
		return Boolean.parseBoolean(getValue("changeOrganization"));
	}
	
	public String getConfDir() {
		if(getValue("confDir")==null) return null;
		return getValue("confDir");
	}
	
	public boolean ignoreCache() {
		if(hasOption("ignoreCache")) return true;
		if(getValue("ignoreCache")==null) return false;
		return Boolean.parseBoolean(getValue("ignoreCache"));
	}

	public List<CacheType> clearCaches() {
		if(getValue("clearCache")==null) return null;
		if(cachesToClear!=null) return cachesToClear;
		cachesToClear = new ArrayList<CacheType>();
		String given = getValue("clearCache");
		for(String cacheName : given.split(",")) {
			if(cacheName.equals("ALL")) cacheName = "*";
			cacheName = cacheName.trim();
			if(cacheName.contains("*")) {
				Pattern pattern = Pattern.compile(cacheName.replace("*", ".*").toLowerCase());
				for(CacheType cacheType : CacheType.values()) {
					Matcher matcher = pattern.matcher(cacheType.name().toLowerCase());
					if(matcher.matches()) {
						cachesToClear.add(cacheType);
					}
				}
			} else {
				try {
					cachesToClear.add(CacheType.valueOf(cacheName));
				} catch (IllegalArgumentException e) {
					LOG.error("Unable to clear cache: " +cacheName + " as the cache is unknown.");
					LOG.error("Available caches: " + Arrays.asList(CacheType.values()));
				}
			}
		}
		return cachesToClear;
	}
	
	
	public void validateRequiredParameters() throws AppException {
		ErrorState errors  = ErrorState.getInstance();
		if(getValue("username")==null && getValue("admin_username")==null) errors.setError("Required parameter: 'username' or 'admin_username' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(getValue("password")==null && getValue("admin_password")==null) errors.setError("Required parameter: 'password' or 'admin_password' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(getValue("host")==null) errors.setError("Required parameter: 'host' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(errors.hasError()) {
			LOG.error("The following parameters: username, password and host are required either using Command-Line-Options or in Environment.Properties");
			LOG.error("       To get help, please use option -h");
			LOG.error("");
			ErrorState.getInstance().setError("Missing required parameters.", ErrorCode.MISSING_PARAMETER, false);
			throw new AppException("Missing required parameters.", ErrorCode.MISSING_PARAMETER);
		}
	}
	
	public boolean hasOption(String key) {
		return ((this.cmd!=null && this.cmd.hasOption(key)) || 
				(this.cmd!=null && this.internalCmd.hasOption(key)) || 
				(this.envProperties!=null && this.envProperties.containsKey(key)) || 
				(this.manualParams!=null && this.manualParams.containsKey(key)));
	}
	
	public String getValue(String key) {
		if(this.cmd!=null && this.cmd.getOptionValue(key)!=null) {
			return this.cmd.getOptionValue(key);
		} else if(this.internalCmd!=null && this.internalCmd.getOptionValue(key)!=null) {
			return this.internalCmd.getOptionValue(key);
		} else if(this.envProperties!=null && this.envProperties.containsKey(key)) {
			return this.envProperties.get(key);
		} else if(this.manualParams!=null && this.manualParams.containsKey(key)) {
			return this.manualParams.get(key);
		} else {
			return null;
		}
	}
	
	public Map<String, String> getEnvironmentProperties() {
		return this.envProperties;
	}

	public void setEnvProperties(EnvironmentProperties envProperties) {
		this.envProperties = envProperties;
	}
}
