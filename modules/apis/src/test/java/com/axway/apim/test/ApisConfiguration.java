package com.axway.apim.test;

import com.axway.apim.EndpointConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({EndpointConfig.class})
public class ApisConfiguration {
    @Bean
    public ImportTestAction importManagerConfigTestAction(){
        return new ImportTestAction();
    }

    @Bean
    public CoreInitializationTestIT coreInitializationTestIT(){
        return new CoreInitializationTestIT();
    }
}
