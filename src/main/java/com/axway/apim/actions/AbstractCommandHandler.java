package com.axway.apim.actions;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.swagger.APIChangeState;

public abstract class AbstractCommandHandler {
	
	static Logger LOG = LoggerFactory.getLogger(AbstractCommandHandler.class);
	
	protected List<RestAPICall> apiCallQueue;
	
	public AbstractCommandHandler() {
		this.apiCallQueue = new Vector<RestAPICall>();
	}

	protected abstract void execute(APIChangeState changes);

	
	
	/*protected void executeAPICalls() {
		Transaction.getInstance().
		Transaction.getInstance().getContext().put("orgId", "0926142d-1049-4847-a1ea-9063d9e1c135");
		for(RestAPICall call : this.apiCallQueue) {
			InputStream response = call.execute();
			call.parseResponse(response);
		}
	}*/
	
	protected void executeAPICall(RestAPICall apiCall) {
		if(apiCall==null) {
			LOG.debug("No API-Call defined ... nothing to do");
			return;
		}
		Transaction transaction = Transaction.getInstance();
		InputStream response = apiCall.execute();
		apiCall.parseResponse(response);
	}
}
