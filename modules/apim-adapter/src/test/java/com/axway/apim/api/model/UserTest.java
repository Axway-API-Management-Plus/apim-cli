package com.axway.apim.api.model;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UserTest {
	@Test
	public void testDifferentUserRole() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setRole("oadmin");
		user2.setRole("admin");
		Assert.assertFalse(user1.deepEquals(user2), "Role of users is different");
	}
	
	@Test
	public void testDifferentUserEmail() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setEmail("mail@customer.com");
		user2.setEmail("other-mail@customer.com");
		Assert.assertFalse(user1.deepEquals(user2), "Email of users is different");
	}
	
	@Test
	public void testDifferentUserName() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setName("My name");
		user2.setName("My name has changed");
		Assert.assertFalse(user1.deepEquals(user2), "Name of users is different");
	}
	
	@Test
	public void testDifferentUserEnabled() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setEnabled(true);
		user2.setEnabled(false);
		Assert.assertFalse(user1.deepEquals(user2), "Enabled status of users is different");
	}
	
	@Test
	public void testDifferentUserMobile() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setMobile("31231232");
		user2.setMobile("65464555");
		Assert.assertFalse(user1.deepEquals(user2), "Enabled status of users is different");
	}
	
	@Test
	public void testDifferentUserCustomProperties() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		HashMap<String, String> customProp1 = new HashMap<>();
		HashMap<String, String> customProp2 = new HashMap<>();
		customProp1.put("customPropA", "Some value");
		customProp2.put("customPropA", "Value has changed");
		user1.setCustomProperties(customProp1);
		user2.setCustomProperties(customProp2);
		Assert.assertFalse(user1.deepEquals(user2), "Custom properties of users is different");
	}
	
	@Test
	public void testDifferentUserDescription() {
		User user1 = getTestUser();
		User user2 = getTestUser();
		user1.setDescription("Initial description");
		user2.setDescription("Changed description");
		Assert.assertFalse(user1.deepEquals(user2), "Description of users is different");
	}
	
	private User getTestUser() {
		User user = new User();
		Organization org = new Organization();
		org.setName("Test-Org");
		user.setOrganization(org);
		user.setEmail("mail@customer.com");
		user.setRole("admin");
		user.setEnabled(true);
		return user;
	}
}
