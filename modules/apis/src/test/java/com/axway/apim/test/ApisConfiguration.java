package com.axway.apim.test;

import com.axway.apim.EndpointConfig;
import org.citrusframework.container.SequenceBeforeSuite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EndpointConfig.class)
public class ApisConfiguration {
//    @Bean
//    public CoreInitializationTestIT coreInitializationTestIT(){
//        return new CoreInitializationTestIT();
//    }

    @Bean
    public Setup setup() {
        return new Setup();
    }

    @Bean
    public SequenceBeforeSuite beforeSuite(Setup setup) {
        return new SequenceBeforeSuite.Builder()
            .actions(setup)
            .build();
    }
}
