package com.axway.apim.cli;

public interface APIMCLIServiceProvider {
	
	/**
	 * This name is used by the CLI in the module summary when executed.
	 * @return the name of the module. Used to show it at execution time.
	 */
	String getName();
	
	/**
	 * This version is used by the CLI in the module summary when executed.
	 * @return the version of the module. Used to show it at execution time.
	 */
	String getVersion();
	
	/**
	 * @return The id of group. Services with the same ID will be grouped into one
	 */
	String getGroupId();
	
	/**
	 * @return the description of the group - The first will be used
	 */
	String getGroupDescription();
}
