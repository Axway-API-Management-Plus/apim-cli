package com.axway.apim.appimport.it;

import com.axway.apim.EndpointConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({EndpointConfig.class})
public class ApplicationsConfiguration {

    @CitrusResource
    private TestContext context;
    @Bean
    public ImportAppTestAction importAppTestAction(){
        return new ImportAppTestAction(context);
    }

    @Bean
    public ExportAppTestAction exportAppTestAction(){
        return new ExportAppTestAction(context);
    }
}
