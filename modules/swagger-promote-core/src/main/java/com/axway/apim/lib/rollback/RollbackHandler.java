package com.axway.apim.lib.rollback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;

public class RollbackHandler {
	
	static Logger LOG = LoggerFactory.getLogger(RollbackHandler.class);
	
	private static RollbackHandler instance;
	
	private List<RollbackAction> rollbackActions;

	private RollbackHandler() {
		super();
		rollbackActions = new ArrayList<RollbackAction>();
	}
	
	public static RollbackHandler getInstance() {
		if(instance==null) {
			instance = new RollbackHandler();
		}
		return instance;
	}	
	
	public static synchronized void deleteInstance () {
		RollbackHandler.instance = null;
	}
	
	public void addRollbackAction(RollbackAction action) {
		rollbackActions.add(action);
	}
	
	public void executeRollback() {
		if(rollbackActions.size()==0) return; // Nothing to roll back
		Collections.sort(rollbackActions, new Comparator<RollbackAction>() {
		    @Override
		    public int compare(RollbackAction first, RollbackAction second) {  
				if(first.getExecuteOrder()>second.getExecuteOrder()) {
					return 1;
				} else {
					return -1;
				}
		    }
		});
		for(RollbackAction action : rollbackActions) {
			try {
				action.rollback();
			} catch (AppException e) {
				LOG.error("Can't rollback ", e);
			}
		}
		LOG.info("Rollback status: '"+rollbackActions+"'");
	}
}
