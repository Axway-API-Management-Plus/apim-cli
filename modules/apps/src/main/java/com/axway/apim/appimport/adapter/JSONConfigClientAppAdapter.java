package com.axway.apim.appimport.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.jackson.AppCredentialsDeserializer;
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
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JSONConfigClientAppAdapter extends ClientAppAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONConfigClientAppAdapter.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	AppImportParams importParams;

	public JSONConfigClientAppAdapter(AppImportParams params) {
		this.importParams = params;
		this.result = new Result();
	}
	
	public JSONConfigClientAppAdapter(AppImportParams params, Result result) {
		this.importParams = params;
		this.result = result;
	}
	
	@Override
	protected void readConfig() throws AppException {
		String config = importParams.getConfig();
		String stage = importParams.getStage();

		File configFile = Utils.locateConfigFile(config);
		if(!configFile.exists()) return;
		File stageConfig = Utils.getStageConfig(stage, importParams.getStageConfig(), configFile);
		List<ClientApplication> baseApps;
		// Try to read a list of applications
		try {
			mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
			mapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(DeserializeMode.configFile)));
			baseApps = mapper.readValue(Utils.substitueVariables(configFile), new TypeReference<List<ClientApplication>>(){});
			if(stageConfig!=null) {
				throw new AppException("Stage overrides are not supported for application lists.", ErrorCode.CANT_READ_CONFIG_FILE);
			} else {
				this.apps = baseApps;
			}
		// Try to read single application
		} catch (MismatchedInputException me) {
			try {
				LOG.debug("Error reading single application: " + me.getMessage() + ". Trying to read single application now.");
				ClientApplication app = mapper.readValue(Utils.substitueVariables(configFile), ClientApplication.class);
				if(stageConfig!=null) {
					try {
						ObjectReader updater = mapper.readerForUpdating(app);
						app = updater.readValue(Utils.substitueVariables(stageConfig));
					} catch (FileNotFoundException e) {
						LOG.warn("No config file found for stage: '"+stage+"'");
					}
				}
				
				this.apps = new ArrayList<ClientApplication>();
				this.apps.add(app);
			} catch (Exception pe) {
				throw new AppException("Cannot read application(s) from config file: " + config + ". Exception: " + pe.getClass().getName() + ": " + pe.getMessage(), ErrorCode.ERR_CREATING_APPLICATION, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read application(s) from config file: " + config + ". Exception: " + e.getClass().getName() + ": " + e.getMessage(), ErrorCode.ERR_CREATING_APPLICATION, e);
		}
		try{
			addImage(apps, configFile.getCanonicalFile().getParentFile());
			addOAuthCertificate(apps, configFile.getCanonicalFile().getParentFile());
		}catch (Exception e){
			throw new AppException("Cannot read image/certificate for application(s) from config file: " + config, ErrorCode.ERR_CREATING_APPLICATION, e);
		}		
		addAPIAccess(apps, result);
		validateCustomProperties(apps);
		validateAppPermissions(apps);
		return;
	}
	
	public ClientApplication getApplication(ClientAppFilter filter) throws AppException {
		return getApplicationByName(filter.getApplicationName());
	}

	private ClientApplication getApplicationByName(String applicationName) throws AppException {
		if(this.apps==null) return null;
		for(ClientApplication app : this.apps) {
			if(applicationName.equals(app.getName())) return app;
		}
		return null;
	}

	private void addImage(List<ClientApplication> apps, File parentFolder) throws AppException {
		for(ClientApplication app : apps) {
			if(app.getImageUrl()==null || app.getImageUrl().equals("")) continue;
			app.setImage(Image.createImageFromFile(new File(parentFolder + File.separator + app.getImageUrl())));
			
		}
	}
	
	private void addOAuthCertificate(List<ClientApplication> apps, File parentFolder) throws AppException {
		for(ClientApplication app : apps) {
			for(ClientAppCredential cred : app.getCredentials()) {
				if(cred instanceof OAuth && ((OAuth) cred).getCert()!=null) {
					File certFile = new File(parentFolder + File.separator +((OAuth) cred).getCert());
					if(!certFile.exists()) {
						throw new AppException("Certificate file: '"+certFile+"' not found.", ErrorCode.UNXPECTED_ERROR);
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
	
	private void addAPIAccess(List<ClientApplication> apps, Result result) throws AppException {
		APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
		for(ClientApplication app : apps) {
			if(app.getApiAccess()==null) continue;
			Iterator<APIAccess> it = app.getApiAccess().iterator();
			while(it.hasNext()) {
				APIAccess apiAccess = it.next();
				List<API> apis = apiAdapter.getAPIs(new APIFilter.Builder()
						.hasName(apiAccess.getApiName())
						.build()
				, false);
				if(apis==null || apis.size()==0) {
					LOG.error("API with name: '" + apiAccess.getApiName() + "' not found. Ignoring this APIs.");
					result.setError(ErrorCode.UNKNOWN_API);
					it.remove();
					continue;
				}
				if(apis.size()>1 && apiAccess.getApiVersion()==null) {
					LOG.error("Found: "+apis.size()+" APIs with name: " + apiAccess.getApiName() + " not providing a version. Ignoring this APIs.");
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
		for(ClientApplication app : apps) {
			Utils.validateCustomProperties(app.getCustomProperties(), Type.application);
		}
	}
	
	private void validateAppPermissions(List<ClientApplication> apps) throws AppException {
		APIManagerUserAdapter userAdapter = APIManagerAdapter.getInstance().userAdapter;		
		for(ClientApplication app: apps) {
			if(app.getPermissions()==null || app.getPermissions().size()==0) continue;
			// First check, if there is an ALL User
			for(ApplicationPermission permission : app.getPermissions()) {
				if("ALL".equals(permission.getUsername())) { 
					// Create a map of all usernames 
					Map<String, ApplicationPermission> usernames = app.getPermissions().stream().collect(
							Collectors.toMap(ApplicationPermission::getUsername, Function.identity()));
					// Get all users for the app organization
					List<User> allOrgUsers = userAdapter.getUsers(new UserFilter.Builder().hasOrganization(app.getOrganization().getName()).build());
					for(User user: allOrgUsers) {
						// Only add permission based on ALL if not manually configured
						if(!usernames.containsKey(user.getLoginName())) {
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
			for(int i=0; i < app.getPermissions().size(); i++) {
				ApplicationPermission permission = app.getPermissions().get(i);
				if(permission.getUser()!=null) continue;
				User user = userAdapter.getUserForLoginName(permission.getUsername());
				if(user==null) {
					LOG.warn("Cannot share application with user: '"+permission.getUsername()+"', as user does not exists.");
					app.getPermissions().remove(i);
					continue;
				}
				permission.setUser(user);
			}
		}
		
	}
}
