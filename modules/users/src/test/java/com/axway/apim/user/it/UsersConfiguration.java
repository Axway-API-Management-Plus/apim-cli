package com.axway.apim.user.it;

import com.axway.apim.EndpointConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({EndpointConfig.class})
public class UsersConfiguration {

    @CitrusResource
    private TestContext context;
    @Bean
    public ImportUserTestAction importUserTestAction(){
        return new ImportUserTestAction(context);
    }

    @Bean
    public ExportUserTestAction exportUserTestAction(){
        return new ExportUserTestAction(context);
    }
}
