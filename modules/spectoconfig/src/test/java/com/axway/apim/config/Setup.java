package com.axway.apim.config;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Setup {

    @BeforeSuite
    private void init() throws IOException, URISyntaxException {
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        String apimCliHome =  Paths.get(uri) + File.separator + "apimcli";
        String confPath = String.valueOf(Files.createDirectories(Paths.get(apimCliHome + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
             OutputStream outputStream = Files.newOutputStream(new File(confPath, "env.properties").toPath())) {
            IOUtils.copy(inputStream, outputStream);
        }
    }
}
