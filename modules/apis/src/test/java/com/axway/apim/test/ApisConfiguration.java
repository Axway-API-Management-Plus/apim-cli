package com.axway.apim.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApisConfiguration {
    @Bean
    public CoreInitializationTestIT coreInitializationTestIT(){
        return new CoreInitializationTestIT();
    }
}
