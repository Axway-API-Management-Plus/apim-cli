package com.axway.apim.apiimport.rollback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;

public interface RollbackAction {
	static Logger LOG = LoggerFactory.getLogger(RollbackAction.class);
	
	public void rollback() throws AppException;
	
	public int getExecuteOrder();
}
