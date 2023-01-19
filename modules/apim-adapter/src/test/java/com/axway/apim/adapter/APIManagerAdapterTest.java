package com.axway.apim.adapter;

import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class APIManagerAdapterTest {


    @Test
    public void testGetHigherRoleAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("admin");
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }
    @Test
    public void testGetHigherRoleOadmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("oadmin");
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserOAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        orgs2Role.put("2038f4db-6453-3d47-8f29-221a057a6e1f", "admin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }
}
