package com.axway.apim.adapter.user;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class APIManagerUserAdapterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

   // APIManagerUserAdapter apiManagerUserAdapter = new APIManagerUserAdapter();

    @Test
    public void getUsers() throws AppException {
        APIManagerAdapter.deleteInstance();
        String loginName = "usera";
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerUserAdapter apiManagerUserAdapter = apiManagerAdapter.userAdapter;
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user =  apiManagerUserAdapter.getUser(userFilter);
        Assert.assertEquals(user.getLoginName(), loginName);
    }

    @Test
    public void deleteUser() throws AppException {
        APIManagerAdapter.deleteInstance();
        String loginName = "usera";
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerUserAdapter apiManagerUserAdapter = apiManagerAdapter.userAdapter;
        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
        User user =  apiManagerUserAdapter.getUser(userFilter);
        try {
            apiManagerUserAdapter.deleteUser(user);
        }catch (AppException appException){
            Assert.fail("unable to delete user", appException);
        }
    }
}
