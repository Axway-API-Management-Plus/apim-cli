package com.axway.apim.users.lib.params;

import com.axway.apim.lib.Parameters;

public class UserChangePasswordParams extends UserExportParams implements Parameters {
	
	private String newPassword;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}	
}
