package com.axway.apim.promote.mvn.plugin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;
import org.mockito.Mock;

import com.axway.apim.promote.mvn.plugin.Publisher;

import java.io.File;

public class PublisherMavenMojoTest extends AbstractMojoTestCase {

    @Mock
    Publisher mojo;


    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws Exception
     *         if any
     */
    @Test
    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/test_pom.xml");

        assertThat(testPom.exists(), is(true));
        mojo = (Publisher) lookupMojo("publish", testPom);
        assertNotNull(mojo);

    }

}

