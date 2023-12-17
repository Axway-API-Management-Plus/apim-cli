package com.axway.apim;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class TestUtils {
    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    public static File createTestDirectory(String testDirName) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File testDir = new File(tmpDir + File.separator + "apim-cli" + File.separator + testDirName);
        if (testDir.mkdirs()) {
            LOG.info("Successfully created Test-Directory: {}", testDir);
        } else {
            LOG.warn("Directory already exists");
        }
        return testDir;
    }


    /**
     * To make testing easier we allow reading test-files from classpath as well
     */
    public static String createTestConfig(String configFilePath, TestContext context, String tempDirName) {
        Resource configFile = Resources.fromClasspath(configFilePath);
        try (Reader reader = configFile.getReader()) {
            String content = IOUtils.toString(reader);
            String replacedConfig = context.replaceDynamicContentInString(content);
            return writeDataToFile(configFile, tempDirName, replacedConfig);
        } catch (IOException e) {
            throw new ValidationException("Unable to create test config file.", e);
        }
    }

    public static String writeDataToFile(Resource configFile, String tempDirName, String replacedConfig){
        File testDirectory = createTestDirectory(tempDirName);
        File newConfigFilePath = new File(testDirectory, configFile.getFile().getName());
        try (Writer writer = new FileWriter(newConfigFilePath)) {
            writer.write(replacedConfig);
            copyTestAssets(configFile.getFile().getParentFile(), testDirectory);
        } catch (IOException e) {
            throw new ValidationException("Unable to create test config file.", e);
        }
        return newConfigFilePath.getAbsolutePath();
    }

    public static void copyTestAssets(File sourceDir, File testDir) {
        if (!sourceDir.exists()) {
            throw new ValidationException("Unable to copy test assets to test directory: '" + testDir + "'. Could not find sourceDir: '" + sourceDir + "'");
        }
        FileFilter filter = new WildcardFileFilter("*.crt", "*.jpg", "*.png", "*.pem");
        try {
            LOG.info("Copy *.crt, *.jpg, *.png, *.pem from source: {} into test-dir: {}", sourceDir, testDir);
            FileUtils.copyDirectory(sourceDir, testDir, filter, true);
        } catch (IOException e) {
            LOG.error("Unable to copy test assets from source: {} into test directory: {}",sourceDir, testDir, e);
        }
    }
}
