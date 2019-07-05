package com.axway.apim.promote.mvn.plugin;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.axway.apim.promote.mvn.plugin.Api;
import com.axway.apim.promote.mvn.plugin.Publication;
import com.axway.apim.promote.mvn.plugin.PublicationReader;
import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigFileIOException;
import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigFileInvalidFormatException;
import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigNotFoundException;

import org.junit.Test;

import com.google.gson.Gson;

import java.io.File;
import java.io.StringReader;
import java.util.Collections;
import java.util.logging.Logger;

public class PublicationReaderTest {
    Logger log = Logger.getLogger(PublicationReaderTest.class.getName());

    @Test
    public void testPublicationReader() throws Exception {

        final String filePath = "src/test/resources/stage_config.json";
        final Publication readObject = new PublicationReader().read(filePath, Publication.class);

        assertThat(readObject, notNullValue());
        assertThat(readObject.getHost(), is("https://test-host.de"));
        assertThat(readObject.getPort(), is(443));
        assertThat(readObject.getApis().size(), is(2));

        readObject.getApis().forEach(e -> {
            assertThat(e.getApiConfig(), notNullValue());
            assertThat(e.getApiSpecification(), notNullValue());
        });
    }

    @Test(expected = StageConfigNotFoundException.class)
    public void testPublicationReaderWithNonExistingFileShouldThrowException()
            throws StageConfigFileIOException, StageConfigNotFoundException, StageConfigFileInvalidFormatException {
        final String filePath = "not_exists.json";
        assertFalse(new File(filePath).exists());
        new PublicationReader().read(filePath, Publication.class);
    }

    @Test(expected = StageConfigFileInvalidFormatException.class)
    public void testPublicationReaderWithWrongFormattedFileShouldThrowException() throws Exception {
        String filePath = "src/test/resources/invalid.json";
        assertTrue(new File(filePath).exists());
        new PublicationReader().read(filePath, Publication.class);
    }

    @Test
    public void testReaderWithUsernameAndPasswordFromSystemPropertiesShouldSuccess()
            throws StageConfigFileIOException, StageConfigNotFoundException, StageConfigFileInvalidFormatException {
        String filePath = "src/test/resources/stage_config.json";
        System.clearProperty("axway.username");
        System.clearProperty("axway.password");
        assertTrue(new File(filePath).exists());
        String username = "property username";
        String password = "property password";
        System.setProperty("axway.username", username);
        System.setProperty("axway.password", password);
        final Publication publication = new PublicationReader().read(filePath, Publication.class);
        assertThat(publication.getUsername(), is(username));
        assertThat(publication.getPassword(), is(password));
    }

    @Test
    public void testReaderWithUsernameAndPasswordInConfigFileShouldSuccess() {
        final Publication publication =
                new Publication().setPort(443).setHost("Test Host").setClientAppsMode(Publication.MODE.IGNORE)
                        .setUsername("abcd").setPassword("efgh").setForce(false).setIgnoreAdminAccount(false)
                        .setIgnoreQuotas(true).setClientOrgsMode(Publication.MODE.IGNORE).setApis(
                        Collections.singletonList(new Api().setApiSpecification("api spec").setApiConfig("apiconfig")));

        final String publicationString1 = new Gson().toJson(publication);

        StringReader stringReader = new StringReader(publicationString1);

        final Publication publication2 = new PublicationReader().read(stringReader, Publication.class);
        assertThat(publication2.getUsername(), is(publication.getUsername()));
        assertThat(publication2.getPassword(), is(publication.getPassword()));

    }

}
