package com.axway.apim.user;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.UserApp;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public class UserAppTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void exportUserByLoginName() {
        String[] args = {"-h", "localhost", "-loginName", "usera"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportUsers() {
        String[] args = {"-h", "localhost"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportUsersJson() {
        String[] args = {"-h", "localhost", "-o", "json", "-deleteTarget"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportUsersYaml() {
        String[] args = {"-h", "localhost", "-o", "yaml", "-deleteTarget"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportUserByName() {
        String[] args = {"-h", "localhost", "-name", "usera"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importUsers() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String userFile = classLoader.getResource("com/axway/apim/users/userImport/user.json").getFile();
        String[] args = {"-h", "localhost", "-c", userFile};
        int returnCode = UserApp.importUsers(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteUsers() {
        String[] args = {"-h", "localhost", "-loginName", "usera"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = UserApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void changePassword() {
        String[] args = {"-h", "localhost", "-loginName", "usera", "-newpassword", Utils.getEncryptedPassword()};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = UserApp.changePassword(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test public void testValue(){
        int code = 1;
        Optional<ErrorCode> errorCode = Arrays.stream(ErrorCode.values())
            .filter(ec -> ec.getCode() == code)
            .findFirst();
       Assert.assertTrue(errorCode.isPresent());
    }
}
