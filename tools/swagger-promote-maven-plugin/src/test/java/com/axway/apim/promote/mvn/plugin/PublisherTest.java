package com.axway.apim.promote.mvn.plugin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.promote.mvn.plugin.Api;
import com.axway.apim.promote.mvn.plugin.AxwayPublishingAdapter;
import com.axway.apim.promote.mvn.plugin.Publication;
import com.axway.apim.promote.mvn.plugin.PublicationReader;
import com.axway.apim.promote.mvn.plugin.Publisher;
import com.axway.apim.promote.mvn.plugin.exceptions.AbstractPublishingPluginException;
import com.axway.apim.promote.mvn.plugin.exceptions.BreakingChangeException;

import java.util.Arrays;

public class PublisherTest {

    @Mock
    AxwayPublishingAdapter axwayPublishingAdapter;

    @Spy
    Publisher sut;

    @Mock
    PublicationReader publicationReader;

    Log log = new SystemStreamLog();

    @Before
    public void setup() {
        sut = new Publisher();
        MockitoAnnotations.initMocks(this);
        AxwayPublishingAdapter.cleanInstance();
        when(sut.getLog()).thenReturn(log);
        when(sut.getPublicationReader()).thenReturn(publicationReader);
        when(sut.getAxwayPublishingAdapter()).thenReturn(axwayPublishingAdapter);
        when(sut.getStageConfigurationFile()).thenReturn("");
        when(sut.getProject()).thenReturn(new MavenProjectStub());
    }

    @Test
    public void testProcessApiGetExceptionWithWarnSeverityShouldNotThrowException()
            throws AppException, MojoExecutionException {
        final Publication publication = createPublication()
                .setApis(Arrays.asList(new Api().setApiSpecification("api1").setApiConfig("config1")));
        when(publicationReader.read(any(String.class), any())).thenReturn(publication);
        doThrow(new AppException("", ErrorCode.NO_CHANGE)).when(axwayPublishingAdapter).processApi(any(), anyString());
        sut.execute();
    }

    @Test(expected = BreakingChangeException.class)
    public void testProcessApiGetExceptionWithErrorSeverityShouldThrowException()
            throws AppException, MojoExecutionException {
        final Publication publication = createPublication()
                .setApis(Arrays.asList(new Api().setApiSpecification("api1").setApiConfig("config1")));
        when(publicationReader.read(any(String.class), any())).thenReturn(publication);
        doThrow(new AppException("", ErrorCode.BREAKING_CHANGE_DETECTED)).when(axwayPublishingAdapter)
                .processApi(any(), anyString());
        sut.execute();
    }

    @Test
    public void testProcessApiWithNoApisShouldNotCallMethodProcessApi() throws MojoExecutionException, AppException {

        when(publicationReader.read(anyString(), any())).thenReturn(createPublication());
        sut.execute();
        verify(axwayPublishingAdapter, Mockito.never()).processApi(any(), anyString());
    }

    @Test
    public void testPublisherWithNApisShouldCallNtimesMethodProcessApi() throws MojoExecutionException, AppException {

        final Publication publication = createPublication().setApis(
                Arrays.asList(new Api().setApiSpecification("api1").setApiConfig("config1"),
                        new Api().setApiSpecification("spec2").setApiConfig("config2"),
                        new Api().setApiSpecification("spec3").setApiConfig("config3")));
        when(publicationReader.read(any(String.class), any())).thenReturn(publication);
        sut.execute();
        verify(axwayPublishingAdapter, times(publication.getApis().size())).processApi(any(), anyString());
    }

    @Test
    public void testPublisherShouldCorrectApisWithAbsolutPath() throws MojoExecutionException, AppException {
        final Publication publication = createPublication()
                .setApis(Arrays.asList(new Api().setApiSpecification("api1").setApiConfig("config1")));
        when(publicationReader.read(any(String.class), any())).thenReturn(publication);
        sut.execute();

        assertThat(publication.getApis().size(), is(1));
        assertThat(publication.getApis().get(0).getApiSpecification(), CoreMatchers.not("api1"));
        assertThat(publication.getApis().get(0).getApiConfig(), CoreMatchers.not("config1"));

    }

    @Test(expected = AbstractPublishingPluginException.class)
    public void testPublisherCallRealprocessMethodShouldThrowAppException() throws MojoExecutionException {

        final Publication publication = createPublication().setApis(
                Arrays.asList(new Api().setApiSpecification("api1").setApiConfig("config1"),
                        new Api().setApiSpecification("spec2").setApiConfig("config2"),
                        new Api().setApiSpecification("spec3").setApiConfig("config3")));
        when(publicationReader.read(any(String.class), any())).thenReturn(publication);
        when(sut.getAxwayPublishingAdapter()).thenReturn(AxwayPublishingAdapter.instance());
        sut.execute();
    }

    private Publication createPublication() {
        return new Publication().setStage("TEST").setUsername("Test Username").setPassword("Test User Password")
                .setPort(443).setHost("Test Host").setClientAppsMode(Publication.MODE.IGNORE).setForce(false)
                .setIgnoreAdminAccount(false).setIgnoreQuotas(true).setClientOrgsMode(Publication.MODE.IGNORE);
    }
}
