package com.axway.apim.test.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIImportConfigAdapter;
import com.axway.apim.api.model.AuthType;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.state.DesiredTestOnlyAPI;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class PrivateKeystoreLoadTest {
	
	AuthenticationProfile profile;
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		new CommandParameters(params);
	}
	
	@BeforeMethod(alwaysRun = true)
	private void setupAuthenticationProfile() {
		profile = new AuthenticationProfile();
		profile.setName("_default");
		profile.setIsDefault(true);
		profile.setType(AuthType.ssl);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("source", "file");
		parameters.put("trustAll", "true");
		parameters.put("password", "axway");
		parameters.put("certFile", "/com/axway/apim/test/files/certificates/clientcert.pfx");
		profile.setParameters(parameters);
	}
	
	@Test
	public void testWorkingKeystoreFile() throws AppException, IOException {
		IAPI testAPI = new DesiredTestOnlyAPI();
		ArrayList<AuthenticationProfile> authnProfiles = new ArrayList<AuthenticationProfile>();
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		// This triggers all post-processing of the DesiredAPI and should not throw an Exception
		importConfig.getDesiredAPI();
	}
	
	@Test
	public void testInvalidPasswordKeystoreFile() throws AppException, IOException {
		IAPI testAPI = new DesiredTestOnlyAPI();
		ArrayList<AuthenticationProfile> authnProfiles = new ArrayList<AuthenticationProfile>();
		profile.getParameters().put("password", "thatswrong");
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		// This triggers all post-processing of the DesiredAPI and should not throw an Exception
		try {
			importConfig.getDesiredAPI();
		} catch(AppException e) {
			Assert.assertTrue(e.getCause().getCause().getMessage().contains("keystore password was incorrect"), 
					"Expected: 'keystore password was incorrect' vs. Actual: '" + e.getCause().getCause().getMessage()+"'");
		}
	}
	
	@Test
	public void testInvalidKeystoreType() throws AppException, IOException {
		IAPI testAPI = new DesiredTestOnlyAPI();
		ArrayList<AuthenticationProfile> authnProfiles = new ArrayList<AuthenticationProfile>();
		profile.getParameters().put("certFile", "/com/axway/apim/test/files/certificates/clientcert.pfx:ABC");
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		try {
			importConfig.getDesiredAPI();
		} catch(AppException e) {
			Assert.assertTrue(e.getCause().getMessage().contains("Unknown keystore type: 'ABC'."), 
					"Expected: 'Unknown keystore type: 'ABC'.' vs. Actual: '" + e.getCause().getMessage()+"'");
		}
	}
	
	@Test
	public void testvalidKeystoreType() throws AppException, IOException {
		IAPI testAPI = new DesiredTestOnlyAPI();
		ArrayList<AuthenticationProfile> authnProfiles = new ArrayList<AuthenticationProfile>();
		profile.getParameters().put("certFile", "/com/axway/apim/test/files/certificates/clientcert.pfx:PKCS12");
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		// This triggers all post-processing of the DesiredAPI and should not throw an Exception
		importConfig.getDesiredAPI();
	}
	
	/* Don't include it by default as it must be executed on Windows :-( 
	@Test
	public void testKeystoreOnCDisk() throws AppException, IOException {
		IAPI testAPI = new DesiredTestAPI();
		ArrayList<AuthenticationProfile> authnProfiles = new ArrayList<AuthenticationProfile>();
		profile.getParameters().setProperty("certFile", "C:\\temp2\\clientcert.pfx");
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		// This triggers all post-processing of the DesiredAPI and should not throw an Exception
		importConfig.getDesiredAPI();
		
		authnProfiles = new ArrayList<AuthenticationProfile>();
		profile.getParameters().setProperty("certFile", "C:\\temp2\\clientcert.pfx:PKCS12");
		authnProfiles.add(profile);
		testAPI.setAuthenticationProfiles(authnProfiles);
		
		importConfig = new APIImportConfigAdapter(testAPI, "justSomething");
		// This triggers all post-processing of the DesiredAPI and should not throw an Exception
		importConfig.getDesiredAPI();
	}*/
}
