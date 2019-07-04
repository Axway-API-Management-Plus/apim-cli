package com.axway.apim.lib.rollback;

public abstract class AbstractRollbackAction implements RollbackAction {
	
	int executeOrder = -1;
	String name;
	
	boolean rolledBack = false;

	public int getExecuteOrder() {
		return executeOrder;
	}

	@Override
	public String toString() {
		return name + " [Rolled back: "+rolledBack+"]";
	}
}
