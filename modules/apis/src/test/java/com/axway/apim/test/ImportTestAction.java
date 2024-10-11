package com.axway.apim.test;

import com.axway.apim.APIImportApp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ImportTestAction extends AbstractTestAction {

    public static String API_DEFINITION = "apiDefinition";
    public static String API_CONFIG = "apiConfig";
    public static String STATE = "state";

    private static final Logger LOG = LoggerFactory.getLogger(ImportTestAction.class);

    File testDir = null;

    @Override
    public void doExecute(TestContext context) {
        String origApiDefinition = context.getVariable(API_DEFINITION);
        String origConfigFile = context.getVariable(API_CONFIG);
        String stage = null;
        String apiDefinition;
        boolean useEnvironmentOnly = false;
        testDir = createTestDirectory(context);
        try {
            stage = context.getVariable("stage");
        } catch (CitrusRuntimeException ignore) {
        }
        if (StringUtils.isNotEmpty(origApiDefinition) && !origApiDefinition.contains("http://") && !origApiDefinition.contains("https://")) {
            apiDefinition = replaceDynamicContentInFile(origApiDefinition, context, createTempFilename(origApiDefinition));
        } else {
            apiDefinition = origApiDefinition;
        }
        String configFile = replaceDynamicContentInFile(origConfigFile, context, createTempFilename(origConfigFile));
        LOG.info("Using Replaced Swagger-File: {}", apiDefinition);
        LOG.info("Using Replaced configFile-File: {}", configFile);
        int expectedReturnCode = 0;
        try {
            expectedReturnCode = Integer.parseInt(context.getVariable("expectedReturnCode"));
        } catch (Exception ignore) {
        }

        try {
            useEnvironmentOnly = Boolean.parseBoolean(context.getVariable("useEnvironmentOnly"));
        } catch (Exception ignore) {
        }

        boolean enforce = false;
        boolean ignoreQuotas = false;
        boolean ignoreCache = false;
        boolean changeOrganization = false;
        String clientOrgsMode = null;
        String clientAppsMode = null;
        String quotaMode = null;
        boolean exportMethods = false;
        boolean useApiAdmin = false;
        try {
            enforce = Boolean.parseBoolean(context.getVariable("enforce"));
        } catch (Exception ignore) {
        }
        try {
            ignoreQuotas = Boolean.parseBoolean(context.getVariable("ignoreQuotas"));
        } catch (Exception ignore) {
        }
        try {
            quotaMode = context.getVariable("quotaMode");
        } catch (Exception ignore) {
        }
        try {
            clientOrgsMode = context.getVariable("clientOrgsMode");
        } catch (Exception ignore) {
        }
        try {
            clientAppsMode = context.getVariable("clientAppsMode");
        } catch (Exception ignore) {
        }

        try {
            changeOrganization = Boolean.parseBoolean(context.getVariable("changeOrganization"));
        } catch (Exception ignore) {
        }
        try {
            ignoreCache = Boolean.parseBoolean(context.getVariable("ignoreCache"));
        } catch (Exception ignore) {
        }

        try {
            exportMethods = Boolean.parseBoolean(context.getVariable("exportMethods"));
        } catch (Exception ignore) {
        }
        try {
            useApiAdmin = Boolean.parseBoolean(context.getVariable("useApiAdmin"));
        } catch (Exception ignore) {
        }

        if (stage == null) {
            stage = "NOT_SET";
        } else {
            // We need to prepare the dynamic staging file used during the test.
            String stageConfigFile = origConfigFile.substring(0, origConfigFile.lastIndexOf(".") + 1) + stage + origConfigFile.substring(origConfigFile.lastIndexOf("."));
            String replacedStagedConfig = configFile.substring(0, configFile.lastIndexOf(".")) + "." + stage + ".json";
            // This creates the dynamic staging config file! (For testing, we also support reading out of a file directly)
            replaceDynamicContentInFile(stageConfigFile, context, replacedStagedConfig);
        }
        copyImagesAndCertificates(origConfigFile);

        List<String> args = new ArrayList<>();
        if (useEnvironmentOnly) {
            args.add("-c");
            args.add(configFile);
            args.add("-s");
            args.add(stage);
        } else {
            args.add("-a");
            args.add(apiDefinition);
            args.add("-c");
            args.add(configFile);
            args.add("-h");
            args.add(context.replaceDynamicContentInString("${apiManagerHost}"));
            args.add("-u");
            if (useApiAdmin) {
                LOG.info("API-Manager import is using user: '" + context.replaceDynamicContentInString("${apiManagerUser}") + "'");
                args.add(context.replaceDynamicContentInString("${apiManagerUser}"));
            } else {
                LOG.info("API-Manager import is using user: '" + context.replaceDynamicContentInString("${oadminUsername1}") + "'");
                args.add(context.replaceDynamicContentInString("${oadminUsername1}"));
            }
            args.add("-p");
            if (useApiAdmin)
                args.add(context.replaceDynamicContentInString("${apiManagerPass}"));
            else
                args.add(context.replaceDynamicContentInString("${oadminPassword1}"));

            args.add("-s");
            args.add(stage);
            if (quotaMode != null) {
                args.add("-quotaMode");
                args.add(quotaMode);
            }
            if (clientOrgsMode != null) {
                args.add("-clientOrgsMode");
                args.add(clientOrgsMode);
            }
            if (clientAppsMode != null) {
                args.add("-clientAppsMode");
                args.add(clientAppsMode);
            }

            args.add("-disableCompression");

            if (changeOrganization) {
                args.add("-changeOrganization");
            }
            if (enforce) {
                args.add("-force");
            }
            if (ignoreQuotas) {
                args.add("-ignoreQuotas");
            }
            if (ignoreCache) {
                args.add("-ignoreCache");
            }
            if (exportMethods) {
                args.add("-exportMethods");
            }
        }
        LOG.info("Enforce breaking change: " + enforce + " | useEnvironmentOnly: " + useEnvironmentOnly);
        int rc = APIImportApp.importAPI(args.toArray(new String[0]));
        if (expectedReturnCode != rc) {
            throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
        }
    }

    /**
     * To make testing easier we allow reading test-files from classpath as well
     */
    private String replaceDynamicContentInFile(String pathToFile, TestContext context, String replacedFilename) {

        File inputFile = new File(pathToFile);
        InputStream is;
        OutputStream os = null;
        try {
            if (inputFile.exists()) {
                is = Files.newInputStream(Paths.get(pathToFile));
                LOG.info("Loading file {} from relative path", pathToFile);
            } else {
                is = this.getClass().getResourceAsStream(pathToFile);
                LOG.info("Loading file {} from class path", pathToFile);

            }
            if (is == null) {
                throw new IOException("Unable to read swagger file from: " + pathToFile);
            }
            String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
            //String filename = pathToFile.substring(pathToFile.lastIndexOf("/")+1); // e.g.: petstore.json, no-change-xyz-config.<stage>.json,

            String jsonReplaced = context.replaceDynamicContentInString(jsonData);

            os = Files.newOutputStream(Paths.get(replacedFilename));
            IOUtils.write(jsonReplaced, os, StandardCharsets.UTF_8);

            return replacedFilename;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    private String createTempFilename(String origFilename) {
        String prefix = origFilename.substring(0, origFilename.indexOf(".") + 1);
        String suffix = origFilename.substring(origFilename.indexOf("."));
        try {
            File tempFile = File.createTempFile(prefix, suffix, testDir);
            tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            LOG.warn("-----> Cannot create temp file. Using unchanged filename!");
            return origFilename;
        }
    }

    private File createTestDirectory(TestContext context) {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
        String apiName = context.getVariable("apiName");
        String testDirName = "ImportActionTest-" + apiName.replace(" ", "") + "-" + randomNum;
        String tmpDir = System.getProperty("java.io.tmpdir");
        File testDir = new File(tmpDir + File.separator + testDirName);
        if (!testDir.mkdir()) {
            randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
            testDirName = "ImportActionTest-" + apiName.replace(" ", "") + "-" + randomNum;
            testDir = new File(tmpDir + File.separator + testDirName);
            if (!testDir.mkdir()) {
                throw new RuntimeException("Failed to create Test-Directory: " + tmpDir + File.separator + testDirName);
            }
        }
        LOG.info("Successfully created Test-Directory: " + tmpDir + File.separator + testDirName);
        return testDir;
    }

    private void copyImagesAndCertificates(String origConfigFile) {
        File sourceDir = new File(origConfigFile).getParentFile();
        if (!sourceDir.exists()) {
            sourceDir = new File(ImportTestAction.class.getResource(origConfigFile).getFile()).getParentFile();
            if (!sourceDir.exists()) {
                return;
            }
        }
        FileFilter filter = new WildcardFileFilter("*.crt", "*.jpg", "*.png", "*.pem", "*.md");
        try {
            LOG.info("Copy certificates and images from source: {} into test-dir: {} (Filter: \"*.crt\", \"*.jpg\", \"*.png\", \"*.pem\", \"*.md\")", sourceDir, testDir);
            FileUtils.copyDirectory(sourceDir, testDir, filter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
