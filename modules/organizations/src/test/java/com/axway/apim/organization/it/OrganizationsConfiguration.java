package com.axway.apim.organization.it;

import com.axway.apim.EndpointConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({EndpointConfig.class})
public class OrganizationsConfiguration {

    @CitrusResource
    private TestContext context;
    @Bean
    public ImportOrganizationTestAction importOrganizationTestAction(){
        return new ImportOrganizationTestAction(context);
    }

    @Bean
    public ExportOrganizationTestAction exportOrganizationTestAction(){
        return new ExportOrganizationTestAction(context);
    }
}
