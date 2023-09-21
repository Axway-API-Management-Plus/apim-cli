package com.axway.apim.adapter.user;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerUserAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;
    private APIManagerUserAdapter apiManagerUserAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            apiManagerUserAdapter = apiManagerAdapter.getUserAdapter();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        apiManagerAdapter.deleteInstance();
        super.close();
    }

    // APIManagerUserAdapter apiManagerUserAdapter = new APIManagerUserAdapter();
    String loginName = "usera";

    @Test
    public void getUsers() throws AppException {
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user = apiManagerUserAdapter.getUser(userFilter);
        Assert.assertEquals(user.getLoginName(), loginName);
    }

    @Test
    public void deleteUser() throws AppException {
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user = apiManagerUserAdapter.getUser(userFilter);
        try {
            apiManagerUserAdapter.deleteUser(user);
        } catch (AppException appException) {
            Assert.fail("unable to delete user", appException);
        }
    }

    @Test
    public void updateUser() throws AppException {
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user = apiManagerUserAdapter.getUser(userFilter);
        User desiredUser = new User();
        desiredUser.setEmail("updated@axway.com");
        desiredUser.setName(user.getName());
        desiredUser.setLoginName(user.getLoginName());
        User newUser = apiManagerUserAdapter.updateUser(desiredUser, user);
        Assert.assertEquals(newUser.getEmail(), "updated@axway.com");
    }

    @Test
    public void updateUserCreateNewUserFlow() throws AppException {
        User user = new User();
        user.setEmail("updated@axway.com");
        user.setName("usera");
        user.setLoginName("usera");
        User newUser = apiManagerUserAdapter.updateUser(user, null);
        Assert.assertEquals(newUser.getEmail(), "updated@axway.com");
    }

    @Test
    public void changePassword() throws AppException {
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user = apiManagerUserAdapter.getUser(userFilter);
        try {
            apiManagerUserAdapter.changePassword(Utils.getEncryptedPassword(), user);
        } catch (AppException appException) {
            Assert.fail("unable to change user password", appException);
        }
    }

    @Test
    public void addImage() throws AppException {
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user = apiManagerUserAdapter.getUser(userFilter);
        user.setImageUrl("https://axway.com/favicon.ico");
        try {
            apiManagerUserAdapter.addImage(user, true);
        } catch (AppException appException) {
            Assert.fail("unable to add Image", appException);
        }
    }
}
