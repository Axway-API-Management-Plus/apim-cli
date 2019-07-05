package com.axway.apim.promote.mvn.plugin;

import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.promote.mvn.plugin.AxwayPublishingAdapter;
import com.axway.apim.promote.mvn.plugin.Publication;
import com.axway.apim.promote.mvn.plugin.exceptions.AbstractPublishingPluginException;
import com.axway.apim.promote.mvn.plugin.exceptions.ArgumentParseException;

public class AxwayPublishingAdapterTest {

    AxwayPublishingAdapter axwayPublishingAdapter;

    @Before
    public void setup() {
        AxwayPublishingAdapter.cleanInstance();
        axwayPublishingAdapter = AxwayPublishingAdapter.instance();
    }


    @Test
    public void testInitOptionsAndArgsShouldNotThrowError()
            throws  AbstractPublishingPluginException {
        final Publication publication = createPublication();
        axwayPublishingAdapter.initOptionsAndArgs(publication);
    }

    @Test(expected = ArgumentParseException.class)
    public void testInitOptionsAndArgsWithMissingArgumentShouldThrowError()
            throws  AbstractPublishingPluginException {
        final Publication publication = createPublication();
        publication.setUsername(null);
        publication.setHost(null);
        axwayPublishingAdapter.initOptionsAndArgs(publication);
    }

    private Publication createPublication() {
        return new Publication().setStage("TEST").setUsername("Test Username").setPassword("Test User Password")
                .setPort(443).setHost("Test Host").setClientAppsMode(Publication.MODE.IGNORE).setForce(false)
                .setIgnoreAdminAccount(false).setIgnoreQuotas(true).setClientOrgsMode(Publication.MODE.IGNORE);
    }
}
