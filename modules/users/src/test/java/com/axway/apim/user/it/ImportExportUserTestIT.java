package com.axway.apim.user.it;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.user.UserExportTestAction;
import com.axway.apim.user.UserImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ImportExportUserTestIT extends TestNGCitrusTestRunner {

	private UserImportTestAction userImport = new UserImportTestAction();
	private UserExportTestAction userExport = new UserExportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/users/userImport/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import user into API-Manager");
		
		variable("userNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("loginName", "My-User-${userNumber}");

		echo("####### Import user: '${loginName}' #######");		
		createVariable(UserImportTestAction.CONFIG,  PACKAGE + "SingleUser.json");
		createVariable("expectedReturnCode", "0");
		userImport.doExecute(context);
		
		echo("####### Validate user: '${loginName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/users?field=loginName&op=eq&value=${loginName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.loginName=='${loginName}')].loginName", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${loginName}')].id", "userId"));
		
		echo("####### Re-Import same user - Should be a No-Change #######");
		createVariable("expectedReturnCode", "10");
		userImport.doExecute(context);
		
		echo("####### Export the user #######");
		ErrorState.deleteInstance();
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		createVariable("expectedReturnCode", "0");
		userExport.doExecute(context);
		
		echo("####### Re-Import EXPORTED user - Should be a No-Change #######");
		createVariable(UserImportTestAction.CONFIG,  "${targetFolder}/${loginName}/user-config.json");
		createVariable("expectedReturnCode", "10");
		userImport.doExecute(context);
	}
}
