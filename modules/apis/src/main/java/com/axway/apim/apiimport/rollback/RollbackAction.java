package com.axway.apim.apiimport.rollback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;

public interface RollbackAction {
	Logger LOG = LoggerFactory.getLogger(RollbackAction.class);
	
	void rollback() throws AppException;
	
	int getExecuteOrder();
}
