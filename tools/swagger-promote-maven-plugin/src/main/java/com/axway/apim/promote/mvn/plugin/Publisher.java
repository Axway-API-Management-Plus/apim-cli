package com.axway.apim.promote.mvn.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.axway.apim.lib.AppException;
import com.axway.apim.promote.mvn.plugin.exceptions.AbstractPublishingPluginException;
import com.axway.apim.promote.mvn.plugin.exceptions.SeverityEnum;

import java.util.List;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "publish", defaultPhase = LifecyclePhase.DEPLOY)
public class Publisher extends AbstractMojo {

    @Parameter(property = "stageConfigurationFile", required = true)
    private String stageConfigurationFile;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The main plugin method to call axway publishing process
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        getLog().info("Starting Axway Publisher");
        AxwayPublishingAdapter adapter = getAxwayPublishingAdapter();
        PublicationReader publicationReader = getPublicationReader();
        try {
            final Publication publication = publicationReader.read(getStageConfigurationFile(), Publication.class);
            initApis(publication.getApis());
            adapter.initOptionsAndArgs(publication);
            getLog().info(publication.toString());

            publish(publication.getApis(), publication.getStage());
        } catch (AbstractPublishingPluginException e) {
            throw e;
        }

    }

    /**
     * Publish every API.
     *
     * @param apis
     * @param stage
     *
     * @throws AbstractPublishingPluginException
     */
    private void publish(final List<Api> apis, final String stage) throws AbstractPublishingPluginException {
        for (final Api api : apis) {
            getLog().info("Processing API " + api.getApiSpecification());
            try {
                getAxwayPublishingAdapter().processApi(api, stage);

            } catch (AppException e) {
                final AbstractPublishingPluginException exception = AppExceptionMapper.map(e);
                switch (SeverityEnum.getSeverityFromErrorCode(exception.getErrorCode())) {
                case INFO:
                    getLog().info(exception.getMessage());
                    break;
                case WARN:
                    getLog().warn(exception.getMessage());
                    break;
                case ERROR:
                    getLog().error(exception.getMessage(), e);
                    throw exception;
                }
            }
        }
    }

    /**
     * Initialize the absolute path of the API spec files and API config files
     *
     * @param apis
     */
    private void initApis(final List<Api> apis) {
        apis.forEach(e -> {
            final String baseDir = getProject().getBasedir().getAbsolutePath();
            e.setApiConfig(String.format("%s/%s", baseDir, e.getApiConfig()));
            e.setApiSpecification(String.format("%s/%s", baseDir, e.getApiSpecification()));
        });
    }

    /**
     * Get the adapter for Axway Publishing.
     * This method is mainly for testing purpose.
     *
     * @return the axwayPublishingAdapter
     */
    protected AxwayPublishingAdapter getAxwayPublishingAdapter() {
        return AxwayPublishingAdapter.instance();
    }

    /**
     * Get the publication reader.
     * This method is mainly for testing purpose.
     *
     * @return the publicationReader
     */
    public PublicationReader getPublicationReader() {
        return new PublicationReader();
    }

    public String getStageConfigurationFile() {
        return stageConfigurationFile;
    }

    public void setStageConfigurationFile(final String stageConfiguationFile) {
        this.stageConfigurationFile = stageConfiguationFile;
    }

    /**
     * This is a getter method which will be used full for uni testing.
     *
     * @return the maven project
     */
    public MavenProject getProject() {
        return project;
    }

    public void setProject(final MavenProject project) {
        this.project = project;
    }

}
