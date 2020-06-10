package com.axway.apim.appimport.adapter;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.appimport.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JSONConfigClientAppAdapter extends ClientAppAdapter {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	List<ClientApplication> apps;

	public JSONConfigClientAppAdapter() {
	}

	@Override
	public boolean readConfig(Object config) throws AppException {
		if (config==null) return false;
		if (config instanceof String == false) return false;
		String myConfig = (String)config;
		File configFile = new File(myConfig);
		if(!configFile.exists()) return false;
		try {
			mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
			this.apps = mapper.readValue(configFile, new TypeReference<List<ClientApplication>>(){});
		} catch (MismatchedInputException me) {
			try {
				ClientApplication app = mapper.readValue(configFile, ClientApplication.class);
				this.apps = new ArrayList<ClientApplication>();
				this.apps.add(app);
			} catch (Exception pe) {
				throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		addImage(apps, configFile.getParentFile());
		addOAuthCertificate(apps, configFile.getParentFile());
		addAPIAccess(apps);
		return true;
	}
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter, boolean logProgress) throws AppException {
		LOG.trace("Filtering results is not supported for the JSON implementation. Returning all applications.");
		return getAllApplications(false);
	}
	
	@Override
	public List<ClientApplication> getAllApplications(boolean logProgress) throws AppException {
		return this.apps;
	}
	
	@Override
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

	@Override
	public ClientApplication createApplication(ClientApplication app) throws AppException {
		throw new UnsupportedOperationException("createApplication not implemented for JSONConfigClientAppAdapter");
	}
	
	@Override
	public ClientApplication updateApplication(ClientApplication desiredApp, ClientApplication actualApp)
			throws AppException {
		throw new UnsupportedOperationException("updateApplication not implemented for JSONConfigClientAppAdapter");
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
	
	private void addAPIAccess(List<ClientApplication> apps) throws AppException {
		APIAdapter apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
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
					LOG.error("API with name: " + apiAccess.getApiName() + " not found. Ignoring this APIs.");
					ErrorState.getInstance().setError("API with name: " + apiAccess.getApiName() + " not found.", ErrorCode.UNKNOWN_API);
					it.remove();
					continue;
				}
				if(apis.size()>1 && apiAccess.getApiVersion()==null) {
					LOG.error("Found: "+apis.size()+" APIs with name: " + apiAccess.getApiName() + " not providing a version. Ignoring this APIs.");
					it.remove();
					ErrorState.getInstance().setError("API with name: " + apiAccess.getApiName() + " not found.", ErrorCode.UNKNOWN_API);
					continue;
				}
				API api = apis.get(0);
				apiAccess.setApiId(api.getId());
			}
		}
	}
}
