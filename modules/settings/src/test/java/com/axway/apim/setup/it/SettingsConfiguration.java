package com.axway.apim.setup.it;

import com.axway.apim.EndpointConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({EndpointConfig.class})
public class SettingsConfiguration {

    @CitrusResource
    private TestContext context;
    @Bean
    public ImportManagerConfigTestAction importManagerConfigTestAction(){
        return new ImportManagerConfigTestAction(context);
    }

    @Bean
    public ExportManagerConfigTestAction exportManagerConfigTestAction(){
        return new ExportManagerConfigTestAction(context);
    }
}
