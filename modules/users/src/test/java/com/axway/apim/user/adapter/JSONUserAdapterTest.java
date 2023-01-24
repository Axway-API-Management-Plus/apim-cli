package com.axway.apim.user.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.adapter.JSONUserAdapter;
import com.axway.apim.users.lib.UserImportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.testng.Assert.*;

public class JSONUserAdapterTest extends WiremockWrapper {

	@BeforeClass
	public void init() {
		initWiremock();
	}

	@AfterClass
	public void stop() {
		close();
	}
		
	private static final String testPackage = "/com/axway/apim/users/adapter";


	@Test
	public void readSingleUserTest() throws AppException {
		String testFile = JSONUserAdapterTest.class.getResource(testPackage + "/SingleUser.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		UserImportParams params = new UserImportParams();
		params.setConfig(testFile);
		params.setUsername("orgadmin");
		params.setPassword(Utils.getEncryptedPassword());
		params.setHostname("localhost");
		JSONUserAdapter adapter = new JSONUserAdapter(params);
		List<User> users = adapter.getUsers();
		assertEquals(users.size(), 1, "Expected 1 user returned from the Adapter");
		User user = users.get(0);
		assertNotNull(user.getImage(), "User should have an image attached");
		assertEquals(user.getImage().getBaseFilename(), "test-user-image.png");
		assertNotNull(user.getOrganization(), "Organization should be initialized");
		///assertNotNull(user.getOrganization().getId(), "Organization should be initialized");
	}
	
	@Test
	public void readManyOrgsTest() throws AppException {
		String testFile = JSONUserAdapterTest.class.getResource(testPackage + "/UserArray.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		UserImportParams params = new UserImportParams();
		params.setUsername("orgadmin");
		params.setPassword(Utils.getEncryptedPassword());
		params.setHostname("localhost");
		params.setConfig(testFile);
		JSONUserAdapter adapter = new JSONUserAdapter(params);
		List<User> users = adapter.getUsers();
		assertEquals(users.size(), 2, "Expected 2 users returned from the Adapter");
	}
	
	@Test
	public void readSingleUserTestWithStagedConfig() throws AppException {
		String testFile = JSONUserAdapterTest.class.getResource(testPackage + "/SingleUser.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		UserImportParams params = new UserImportParams();
		params.setConfig(testFile);
		params.setStageConfig("StagedSingleUser.json");
		params.setUsername("orgadmin");
		params.setPassword(Utils.getEncryptedPassword());
		params.setHostname("localhost");
		JSONUserAdapter adapter = new JSONUserAdapter(params);
		List<User> users = adapter.getUsers();
		assertEquals(users.size(), 1, "Expected 1 user returned from the Adapter");
		User user = users.get(0);
		assertEquals(user.getLoginName(), "thomas");
		assertEquals(user.getName(), "User Chris");
	}
	
	@Test
	public void readSingleUserTestWithStage() throws AppException {
		String testFile = JSONUserAdapterTest.class.getResource(testPackage + "/SingleUser.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		UserImportParams params = new UserImportParams();
		params.setConfig(testFile);
		params.setStage("prod");
		params.setUsername("orgadmin");
		params.setPassword(Utils.getEncryptedPassword());
		params.setHostname("localhost");
		JSONUserAdapter adapter = new JSONUserAdapter(params);
		List<User> users = adapter.getUsers();
		assertEquals(users.size(), 1, "Expected 1 user returned from the Adapter");
		User user = users.get(0);
		assertEquals(user.getLoginName(), "prod-chris");
		assertEquals(user.getName(), "User Chris");
	}
}