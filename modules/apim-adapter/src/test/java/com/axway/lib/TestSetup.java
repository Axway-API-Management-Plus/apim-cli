package com.axway.lib;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSetup {

    @BeforeSuite
    public void initCliHome() throws IOException, URISyntaxException {
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        String path = Paths.get(uri) + File.separator + "apimcli";
        System.out.println(path);
        String confPath = String.valueOf(Files.createDirectories(Paths.get(path + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
             OutputStream outputStream = Files.newOutputStream(new File(confPath, "env.properties").toPath())) {
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.stageWithProxy.properties");
             OutputStream outputStream = Files.newOutputStream(new File(confPath, "env.stageWithProxy.properties").toPath())) {
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.yetAnotherStage.properties");
             OutputStream outputStream = Files.newOutputStream(new File(confPath, "env.yetAnotherStage.properties").toPath())) {
            IOUtils.copy(inputStream, outputStream);
        }
    }
}
