package com.axway.apim.adapter.apis;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.user.UserFilter;

public class UserFilterTest {
	@Test
	public void emptyUserFilter() {
		UserFilter filter1 = new UserFilter.Builder().build();
		UserFilter filter2 = new UserFilter.Builder().build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "Something");
		testMap.put(filter2, "Something else");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "Something else", "Which filter instance to use should not make a difference");
	}
	
	@Test
	public void userFilterWithDifferentLoginName() {
		UserFilter filter1 = new UserFilter.Builder().hasLoginName("ABC").build();
		UserFilter filter2 = new UserFilter.Builder().hasLoginName("XYZ").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "LoginName for ABC");
		testMap.put(filter2, "LoginName for XYZ");
		
		Assert.assertEquals(testMap.size(), 2, "We expect two entries as the filter is different");
		Assert.assertEquals(testMap.get(filter1), "LoginName for ABC");
		Assert.assertEquals(testMap.get(filter2), "LoginName for XYZ");
	}
	
	@Test
	public void UserFilterWithSameLoginName() {
		UserFilter filter1 = new UserFilter.Builder().hasLoginName("ABC").build();
		UserFilter filter2 = new UserFilter.Builder().hasLoginName("ABC").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "LoginName for ABC");
		testMap.put(filter2, "LoginName for XYZ");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only ONE as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "LoginName for XYZ", "XYZ must be returned as it was put last");
	}
	
	@Test
	public void UserFilterWithDifferentId() {
		UserFilter filter1 = new UserFilter.Builder().hasId("1234567890").build();
		UserFilter filter2 = new UserFilter.Builder().hasId("0987654321").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect two entries as the filter is different");
		Assert.assertEquals(testMap.get(filter1), "Name for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321");
	}
	
	@Test
	public void UserFilterWithSameId() {
		UserFilter filter1 = new UserFilter.Builder().hasId("1234567890").build();
		UserFilter filter2 = new UserFilter.Builder().hasId("1234567890").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321", "0987654321 must be returned as it was put last");
	}
	
	@Test
	public void UserFilterWithSameIdAndLoginName() {
		UserFilter filter1 = new UserFilter.Builder().hasId("1234567890").hasLoginName("ABC").build();
		UserFilter filter2 = new UserFilter.Builder().hasId("1234567890").hasLoginName("ABC").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "LoginName for 1234567890");
		testMap.put(filter2, "LoginName for 0987654321");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "LoginName for 0987654321", "0987654321 must be returned as it was put last");
	}
	
	@Test
	public void UserFilterWithDifferentIdAndName() {
		UserFilter filter1 = new UserFilter.Builder().hasId("1234567890").hasLoginName("ABC").build();
		UserFilter filter2 = new UserFilter.Builder().hasId("0987654321").hasLoginName("XYZ").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "LoginName for 1234567890");
		testMap.put(filter2, "LoginName for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "LoginName for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "LoginName for 0987654321");
	}
	
	@Test
	public void UserFilterWithDifferentIdSameName() {
		UserFilter filter1 = new UserFilter.Builder().hasId("1234567890").hasLoginName("ABC").build();
		UserFilter filter2 = new UserFilter.Builder().hasId("0987654321").hasLoginName("ABC").build();
		Map<UserFilter, String> testMap = new HashMap<UserFilter, String>();
		testMap.put(filter1, "LoginName for 1234567890");
		testMap.put(filter2, "LoginName for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "LoginName for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "LoginName for 0987654321");
	}
	
	@Test
	public void filterUserWithLoginName() {
		UserFilter filter = new UserFilter.Builder()
				.hasLoginName("myLoginName")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "loginName");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "myLoginName");
	}
	
	@Test
	public void filterUserWithLoginNameWildcard() {
		UserFilter filter = new UserFilter.Builder()
				.hasLoginName("*LoginABC*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "loginName");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "like");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "LoginABC");
	}
	
	@Test
	public void filterUserWithName() {
		UserFilter filter = new UserFilter.Builder()
				.hasName("myName")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "name");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "myName");
	}
	
	@Test
	public void filterUserWithNameWildcard() {
		UserFilter filter = new UserFilter.Builder()
				.hasName("*NameABC*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "name");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "like");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "NameABC");
	}
	
	@Test
	public void filterUserEnabledTrue() {
		UserFilter filter = new UserFilter.Builder()
				.isEnabled(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "enabled");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "enabled");
	}
	
	@Test
	public void filterUserRoleAdmin() {
		UserFilter filter = new UserFilter.Builder()
				.hasRole("admin")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "role");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "admin");
	}
	
	@Test
	public void filterUserRoleOAdmin() {
		UserFilter filter = new UserFilter.Builder()
				.hasRole("oadmin")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "role");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "oadmin");
	}
	
	@Test
	public void filterUserRoleUser() {
		UserFilter filter = new UserFilter.Builder()
				.hasRole("user")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "role");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "user");
	}
	
	@Test
	public void filterEnabledUsers() {
		UserFilter filter = new UserFilter.Builder()
				.isEnabled(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "enabled");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "enabled");
	}
	
	@Test
	public void filterDisabledUsers() {
		UserFilter filter = new UserFilter.Builder()
				.isEnabled(false)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "enabled");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "disabled");
	}
	
	@Test
	public void filterUserEmail() {
		UserFilter filter = new UserFilter.Builder()
				.hasEmail("MARK@axway.com")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "email");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "mark@axway.com");
	}
	
	@Test
	public void filterUserEmailWildcard() {
		UserFilter filter = new UserFilter.Builder()
				.hasEmail("*@Axway*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "email");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "like");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "@axway");
	}
	
	
	@Test
	public void filterUserId() {
		UserFilter filter = new UserFilter.Builder()
				.hasId("123456789")
				.build();
		Assert.assertEquals(filter.getId(), "123456789");
	}
	
	@Test
	public void filterUserWithDescription() {
		UserFilter filter = new UserFilter.Builder()
				.hasDescription("my description I want")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "description");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "like");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "my description I want");
	}
}
