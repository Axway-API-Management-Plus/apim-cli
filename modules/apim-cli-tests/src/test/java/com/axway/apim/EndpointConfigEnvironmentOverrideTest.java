package com.axway.apim;

import com.axway.apim.EndpointConfig.GlobalVariableEnvironmentOverrides;
import org.citrusframework.variable.GlobalVariables;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * The integration test credentials come from environment variables, while citrus-global-variables.properties only
 * holds the defaults. The file cannot use ${...} placeholders, because the Citrus GlobalVariablesPropertyLoader
 * reads that same file and resolves ${...} as a Citrus test variable, failing on anything it does not know.
 */
public class EndpointConfigEnvironmentOverrideTest {

    private static StandardEnvironment environmentWith(Map<String, Object> properties) {
        StandardEnvironment environment = new StandardEnvironment();
        // Stands in for the citrus-global-variables.properties defaults.
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("apiManagerHost", "localhost");
        defaults.put("apiManagerPass", "");
        defaults.put("oadminPassword1", "");
        defaults.put("apiManagerUser", "apiadmin");
        environment.getPropertySources().addLast(new MapPropertySource("defaults", defaults));
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("environment", properties));
        }
        return environment;
    }

    @Test
    public void environmentVariablesOverrideTheDefaults() {
        Map<String, Object> env = new HashMap<>();
        env.put("apimanager_host", "apim.example.com");
        env.put("apimanager_password", "secret-from-environment");
        env.put("oadmin_password", "oadmin-from-environment");
        StandardEnvironment environment = environmentWith(env);

        new EndpointConfig().setEnvironment(environment);

        Assert.assertEquals(environment.getProperty("apiManagerHost"), "apim.example.com");
        Assert.assertEquals(environment.getProperty("apiManagerPass"), "secret-from-environment");
        Assert.assertEquals(environment.getProperty("oadminPassword1"), "oadmin-from-environment");
        Assert.assertEquals(environment.getProperty("apiManagerUser"), "apiadmin", "Untouched properties must keep their default");
    }

    @Test
    public void defaultsSurviveWhenNoEnvironmentVariableIsSet() {
        StandardEnvironment environment = environmentWith(new HashMap<String, Object>());

        new EndpointConfig().setEnvironment(environment);

        Assert.assertEquals(environment.getProperty("apiManagerHost"), "localhost");
        Assert.assertEquals(environment.getProperty("apiManagerPass"), "");
    }

    /**
     * An empty environment variable must not win over the default, otherwise an unset CI secret would silently
     * blank out the host.
     */
    @Test
    public void emptyEnvironmentVariableDoesNotOverrideTheDefault() {
        Map<String, Object> env = new HashMap<>();
        env.put("apimanager_host", "");
        StandardEnvironment environment = environmentWith(env);

        new EndpointConfig().setEnvironment(environment);

        Assert.assertEquals(environment.getProperty("apiManagerHost"), "localhost");
    }

    /**
     * The Citrus global variables are filled straight from the file by the property loader, so they have to be
     * re-read through the environment to pick the overrides up.
     */
    @Test
    public void citrusGlobalVariablesPickUpTheOverrides() {
        Map<String, Object> env = new HashMap<>();
        env.put("apimanager_password", "secret-from-environment");
        StandardEnvironment environment = environmentWith(env);
        new EndpointConfig().setEnvironment(environment);

        GlobalVariables globalVariables = new GlobalVariables();
        // What GlobalVariablesPropertyLoader would have loaded from the file.
        globalVariables.getVariables().put("apiManagerPass", "");
        globalVariables.getVariables().put("apiManagerUser", "apiadmin");

        new GlobalVariableEnvironmentOverrides(globalVariables, environment);

        Assert.assertEquals(globalVariables.getVariables().get("apiManagerPass"), "secret-from-environment");
        Assert.assertEquals(globalVariables.getVariables().get("apiManagerUser"), "apiadmin");
    }
}
