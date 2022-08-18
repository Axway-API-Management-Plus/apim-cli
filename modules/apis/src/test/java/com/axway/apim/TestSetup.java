package com.axway.apim;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSetup {

    @BeforeSuite
    public void initCliHome() throws IOException {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
        String confPath = String.valueOf(Files.createDirectories(Paths.get(path + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
           OutputStream outputStream= Files.newOutputStream(new File(confPath, "env.properties").toPath())){
            IOUtils.copy(inputStream,outputStream );
        }
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.anyOtherStage.properties");
             OutputStream outputStream= Files.newOutputStream(new File(confPath, "env.anyOtherStage.properties").toPath())){
            IOUtils.copy(inputStream,outputStream );
        }

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.variabletest.properties");
             OutputStream outputStream= Files.newOutputStream(new File(confPath, "env.variabletest.properties").toPath())){
            IOUtils.copy(inputStream,outputStream );
        }
    }
}
