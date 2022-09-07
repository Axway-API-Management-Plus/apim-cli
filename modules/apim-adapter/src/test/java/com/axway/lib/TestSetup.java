package com.axway.lib;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSetup {

    @BeforeSuite
    public void initCliHome() throws IOException, URISyntaxException {
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        String path =  Paths.get(uri) + File.separator + "apimcli";
        String confPath = String.valueOf(Files.createDirectories(Paths.get(path + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
           OutputStream outputStream=new FileOutputStream(new File(confPath, "env.properties" ))){
            IOUtils.copy(inputStream,outputStream );
        }
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.stageWithProxy.properties");
             OutputStream outputStream=new FileOutputStream(new File(confPath, "env.stageWithProxy.properties" ))){
            IOUtils.copy(inputStream,outputStream );
        }
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.yetAnotherStage.properties");
             OutputStream outputStream=new FileOutputStream(new File(confPath, "env.yetAnotherStage.properties" ))){
            IOUtils.copy(inputStream,outputStream );
        }
    }


}
