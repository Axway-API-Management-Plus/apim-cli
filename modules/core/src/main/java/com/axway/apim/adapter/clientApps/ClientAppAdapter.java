package com.axway.apim.adapter.clientApps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class ClientAppAdapter {
	
	protected boolean includeQuota;
	
	protected String applicationName;
	
	protected String applicationId;
	
	protected String organization;
	
	protected String state;

	protected ClientAppAdapter() {

	}
	
	/**
	 * Returns a list of ClientApplications from the Adpater
	 * @return list of ClientApplications
	 * @throws AppException when something goes wrong
	 */
	public abstract List<ClientApplication> getApplications() throws AppException;
	
	public abstract ClientApplication getApplication(String applicationName) throws AppException;
	
	public abstract boolean readConfig(Object config) throws AppException;
	
	public void setIncludeQuota(boolean includeQuota) {
		this.includeQuota = includeQuota;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public void setState(String state) {
		this.state = state;
	}


	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		private static Logger LOG = LoggerFactory.getLogger(Builder.class);
		
		private List<ClientAppAdapter> appProvider = new ArrayList<ClientAppAdapter>();
		
		Object config;
		
		boolean includeQuota;
		
		String organization;
		
		/** The name of the application */
		String applicationName;
		
		String applicationId;
		
		String state;
		
		/**
		 * @param config the config that is used what kind of adapter should be used
		 */
		public Builder(Object config) {
			super();
			this.config = config;
			ServiceLoader<ClientAppAdapter> loader = ServiceLoader.load(ClientAppAdapter.class);
			Iterator<ClientAppAdapter> it = loader.iterator();
			while(it.hasNext()) {
				ClientAppAdapter nextProvider = it.next();
				appProvider.add(nextProvider);
			}
		}

		/**
		 * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
		 * @return a valid Adapter able to handle the config or null
		 */
		public ClientAppAdapter build() {
			for(ClientAppAdapter adapter : this.appProvider) {
				try {
					if(!adapter.readConfig(this.config)) continue;
				} catch (AppException e) {
					LOG.debug("Can't read config: "+this.config+" with adapter: " + adapter.getClass().getName() + " " + e.getMessage());
					e.printStackTrace();
				}
				adapter.setIncludeQuota(includeQuota);
				adapter.setApplicationName(applicationName);
				adapter.setApplicationId(applicationId);
				adapter.setOrganization(organization);
				adapter.setState(state);
				return adapter;
			}
			return null;
		}
		
		public Builder includeQuotas(boolean includeQuota) {
			this.includeQuota = includeQuota;
			return this;
		}
		
		public Builder hasName(String name) {
			this.applicationName = name;
			return this;
		}
		
		public Builder hasId(String id) {
			this.applicationId = id;
			return this;
		}
		
		public Builder hasOrganization(String organization) {
			this.organization = organization;
			return this;
		}
		
		public Builder hasState(String state) {
			this.state = state;
			return this;
		}
	}
}
