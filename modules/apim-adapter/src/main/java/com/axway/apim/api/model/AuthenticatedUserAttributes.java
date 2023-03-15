package com.axway.apim.api.model;

public class AuthenticatedUserAttributes {

    private boolean firstLogin;
    private boolean isSSOLogin;
    private String userDn;
    private Long lastSeen;
    private boolean changePassword;
    private String changePasswordMessage;


    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public boolean isSSOLogin() {
        return isSSOLogin;
    }

    public void setSSOLogin(boolean isSSOLogin) {
        this.isSSOLogin = isSSOLogin;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isChangePassword() {
        return changePassword;
    }

    public void setChangePassword(boolean changePassword) {
        this.changePassword = changePassword;
    }

    public String getChangePasswordMessage() {
        return changePasswordMessage;
    }

    public void setChangePasswordMessage(String changePasswordMessage) {
        this.changePasswordMessage = changePasswordMessage;
    }
}
