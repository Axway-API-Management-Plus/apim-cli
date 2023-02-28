package com.axway.apim.adapter.client.apps;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;

public abstract class ClientAppAdapter {
	
	protected static Logger LOG = LoggerFactory.getLogger(ClientAppAdapter.class);
	
	protected List<ClientApplication> apps;
	
	protected Result result;

	protected ClientAppAdapter() {

	}
	
	/**
	 * Returns a list of application according to the provided filter
	 * @return applications according to the provided filter
	 * @throws AppException when something goes wrong
	 */
	public List<ClientApplication> getApplications() throws AppException {
		if(this.apps==null) readConfig();
		return this.apps;
	}
	
	protected abstract void readConfig() throws AppException;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}
}
