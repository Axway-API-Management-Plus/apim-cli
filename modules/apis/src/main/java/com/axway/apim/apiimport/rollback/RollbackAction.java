package com.axway.apim.apiimport.rollback;

import com.axway.apim.lib.error.AppException;

public interface RollbackAction {
	void rollback() throws AppException;
	
	int getExecuteOrder();
}
