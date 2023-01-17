package com.axway.apim.user.adapter.lib;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.users.UserApp;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserAppTest  extends WiremockWrapper {

    private String userConfig;

    @BeforeClass
    public void init() throws URISyntaxException {
        initWiremock();
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        Path path = Paths.get(uri);
        userConfig = path + File.separator  + "com/axway/apim/users/userImport/user.json";

    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void exportUsers(){
        String[] args = {"-h", "localhost", "-loginName", "usera"};
        int returnCode = UserApp.export(args);
        Assert.assertEquals(0, returnCode);
    }

    @Test
    public void importUsers(){
        String[] args = {"-h", "localhost", "-loginName", "apiadmin", "-c", userConfig};
        int returnCode = UserApp.importUsers(args);
        Assert.assertEquals(0, returnCode);
    }

}
