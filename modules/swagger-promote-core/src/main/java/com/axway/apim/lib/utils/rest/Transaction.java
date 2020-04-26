package com.axway.apim.lib.utils.rest;

import java.util.HashMap;

/**
 * Helper class which can be used for any purpose to store information and to be pulled out later. 
 * It's a Singleton and created when the tools logs-in for the first time into the API-Manager.
 * 
 * @author cwiechmann@axway.com
 */
public class Transaction {
	private static Transaction instance;
	
	private HashMap<Object, Object> context = new HashMap<Object, Object>();
	
	private Transaction() {}
	
	public static synchronized Transaction getInstance () {
		if (Transaction.instance == null) {
			Transaction.instance = new Transaction ();
		}
		return Transaction.instance;
	}
	
	public static synchronized void deleteInstance () {
		Transaction.instance = null;
	}
	
	public void beginTransaction() {
		this.context.clear();
	}
	public void stopTransaction() {
		this.context.clear();
	}
	
	public void put(Object key, Object value) {
		this.context.put(key, value);
	}
	
	public Object get(Object key) {
		return this.context.get(key);
	}	
}
