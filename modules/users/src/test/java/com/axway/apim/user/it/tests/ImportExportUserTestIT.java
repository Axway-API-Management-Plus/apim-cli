package com.axway.apim.user.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.users.UserApp;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;


@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportExportUserTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void runImportUser(@Optional @CitrusResource TestContext context) {
        description("Import user into API-Manager incl. custom properties");
        variable("loginName", "citrus:concat('My-User-',  citrus:randomNumber(4))");
        variable("password", "changeme");
        variable("phone", "+006856778789");
        variable("mobile", "+534534534435");
        variable("userCustomProperty1", "User custom value 1");
        variable("userCustomProperty2", "2");
        variable("userCustomProperty3", "true");
        $(echo("####### Import user: '${loginName}' having custom properties and a password #######"));

        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/users/userImport/SingleUser.json", context, "users", true);
        $(testContext -> {
            String[] args = {"user", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = UserApp.importUsers(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate user: '${loginName}' has been imported incl. custom properties and the given password #######"));

        $(http().client(apiManager).send().get("/users?field=loginName&op=eq&value=${loginName}").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.loginName=='${loginName}')].loginName", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.loginName=='${loginName}')].userCustomProperty1", "User custom value 1")
            .expression("$.[?(@.loginName=='${loginName}')].userCustomProperty2", "2")
            .expression("$.[?(@.loginName=='${loginName}')].userCustomProperty3", "true")
            .expression("$.[?(@.loginName=='${loginName}')].phone", "+006856778789")
            .expression("$.[?(@.loginName=='${loginName}')].mobile", "+534534534435")).extract(fromBody()
            .expression("$.[?(@.loginName=='${loginName}')].id", "userId")));
    }
}
