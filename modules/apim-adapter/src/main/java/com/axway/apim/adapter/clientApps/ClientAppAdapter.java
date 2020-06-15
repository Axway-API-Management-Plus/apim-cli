package com.axway.apim.adapter.clientApps;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class ClientAppAdapter {
	
	protected static Logger LOG = LoggerFactory.getLogger(ClientAppAdapter.class);

	protected ClientAppAdapter() {

	}
	
	/**
	 * Returns a list of application according to the provided filter
	 * @return applications according to the provided filter
	 * @throws AppException when something goes wrong
	 */
	public abstract List<ClientApplication> getApplications() throws AppException;
	
	public abstract boolean readConfig(Object config) throws AppException;

	
	public static ClientAppAdapter create(Object config){
		ServiceLoader<ClientAppAdapter> loader = ServiceLoader.load(ClientAppAdapter.class);
		Iterator<ClientAppAdapter> it = loader.iterator();
		while(it.hasNext()) {
			ClientAppAdapter provider = it.next();
			try {
				if(!provider.readConfig(config)) continue;
				return provider;
			} catch (AppException e) {
				LOG.debug("Can't read config: "+config+" with adapter: " + provider.getClass().getName() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
}
