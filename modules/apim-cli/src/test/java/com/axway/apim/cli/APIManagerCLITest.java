package com.axway.apim.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class APIManagerCLITest {

    @Test
    public void testArgsIsNull() {
        APIManagerCLI cli = new APIManagerCLI(null);
        Assert.assertNull(cli.selectedService);
        Assert.assertNull(cli.selectedMethod);
    }

    @Test
    public void testEmptyArgs() {
        APIManagerCLI cli = new APIManagerCLI(new String[]{});
        // These tests required to have the app- and api modules available in the classpath
        Assert.assertTrue(cli.servicesMappedByGroup instanceof Map);
        Assert.assertTrue(cli.servicesMappedByGroup.containsKey("api"));
        Assert.assertTrue(cli.servicesMappedByGroup.containsKey("app"));
        Assert.assertTrue(cli.servicesMappedByGroup.get("api").size() == 2);
        Assert.assertNull(cli.selectedService);
        Assert.assertNull(cli.selectedMethod);
    }

    @Test
    public void testWithServiceIdOnly() {
        APIManagerCLI cli = new APIManagerCLI(new String[]{"api"});
        Assert.assertNotNull(cli.selectedServiceGroup);
        Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
        Assert.assertNull(cli.selectedMethod);
    }

    @Test
    public void testWithServiceIdAndMethod() {
        APIManagerCLI cli = new APIManagerCLI(new String[]{"api", "import"});
        Assert.assertNotNull(cli.selectedServiceGroup);
        Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
        Assert.assertNotNull(cli.selectedMethod);
        Assert.assertTrue(cli.selectedService instanceof APIMCLIServiceProvider, "cli.selectedService is type: " + cli.selectedService.getClass().getName());
        Assert.assertTrue(cli.selectedMethod instanceof Method);
        Assert.assertEquals(cli.selectedMethod.getName(), "importAPI");
    }

    @Test
    public void simulateChocoExecution() {
        APIManagerCLI cli = new APIManagerCLI(new String[]{"choco", "api", "import"});
        Assert.assertNotNull(cli.selectedServiceGroup);
        Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
        Assert.assertNotNull(cli.selectedMethod);
        Assert.assertTrue(cli.selectedService instanceof APIMCLIServiceProvider, "cli.selectedService is type: " + cli.selectedService.getClass().getName());
        Assert.assertEquals(cli.selectedMethod.getName(), "importAPI");
    }

    @Test
    public void testRunWithoutArguments() {
        APIManagerCLI apiManagerCLI = new APIManagerCLI(null);
        int returnCode = apiManagerCLI.run(null);
        Assert.assertEquals(0, returnCode);
    }

    @Test
    public void testRun() {
        String[] args = new String[]{"api", "import"};
        APIManagerCLI apiManagerCLI = new APIManagerCLI(args);
        int returnCode = apiManagerCLI.run(args);
        Assert.assertEquals(0, returnCode);
    }
    @Test
    public void testRunInvalidCommand() {
        String[] args = new String[]{"api", "desc"};
        APIManagerCLI apiManagerCLI = new APIManagerCLI(args);
        int returnCode = apiManagerCLI.run(args);
        Assert.assertEquals(0, returnCode);
    }

    @Test
    public void testRunWithException() {
        String[] args = new String[]{"api", "delete"};
        APIManagerCLI apiManagerCLI = new APIManagerCLI(args);
        int returnCode = apiManagerCLI.run(args);
        Assert.assertEquals(99, returnCode);
    }

    /**
     * Errors thrown by a command (e.g. a NoSuchMethodError caused by mixed up library versions) must not get lost
     * silently, see issue #582.
     */
    @Test
    public void testErrorThrownByCommandIsLogged() throws Exception {
        APIManagerCLI cli = new APIManagerCLI(new String[]{"api", "import"});
        cli.selectedService = new FailingService();
        cli.selectedMethod = FailingService.class.getMethod("failingCommand", String[].class);

        List<String> messages = new ArrayList<>();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        AbstractAppender appender = new AbstractAppender("issue582", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                String thrown = event.getThrown() != null ? " " + event.getThrown() : "";
                messages.add(event.getMessage().getFormattedMessage() + thrown);
            }
        };
        appender.start();
        context.getConfiguration().getRootLogger().addAppender(appender, null, null);
        context.updateLoggers();
        int returnCode;
        try {
            returnCode = cli.run(new String[]{"api", "import"});
        } finally {
            context.getConfiguration().getRootLogger().removeAppender("issue582");
            context.updateLoggers();
            appender.stop();
        }
        Assert.assertEquals(returnCode, 99);
        Assert.assertTrue(messages.stream().anyMatch(m -> m.contains("NoSuchMethodError") && m.contains("booleanSchemaValue")),
            "The error thrown by the command must be logged. Logged: " + messages);
    }

    public static class FailingService implements APIMCLIServiceProvider {
        @CLIServiceMethod(name = "failing", description = "Fails with an Error")
        public int failingCommand(String[] args) {
            throw new NoSuchMethodError("booleanSchemaValue(Boolean)");
        }

        @Override
        public String getName() {
            return "Failing service";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String getGroupId() {
            return "failing";
        }

        @Override
        public String getGroupDescription() {
            return "Failing service";
        }
    }
}
