package com.axway.apim.appimport.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.appimport.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
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
		return true;
	}
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter) throws AppException {
		throw new UnsupportedOperationException("Filtering results is not supported for the JSON implementation");
	}

	@Override
	public ClientApplication getApplication(ClientApplication applicationName) throws AppException {
		if(this.apps==null) return null;
		for(ClientApplication app : this.apps) {
			if(applicationName.getName().equals(app.getName())) return app;
		}
		return null;
	}

	@Override
	public ClientApplication createApplication(ClientApplication app) throws AppException {
		throw new UnsupportedOperationException("createApplication not implemented for JSONConfigClientAppAdapter");
	}

	@Override
	public List<ClientApplication> getApplications() throws AppException {
		return this.apps;
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
}
