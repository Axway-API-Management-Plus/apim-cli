package com.axway.apim.actions.rest;

import java.util.HashMap;

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
