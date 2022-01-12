package com.axway.apim.users.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardImportParams;

public class UserImportParams extends StandardImportParams {
	
	public static synchronized UserImportParams getInstance() {
		return (UserImportParams)CoreParameters.getInstance();
	}
}
