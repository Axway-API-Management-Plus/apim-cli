package com.axway.apim.users.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.users.lib.params.UserExportParams;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsoleUserExporter extends UserResultHandler {

    public static final String ENABLED = "Enabled";
    public static final String EMAIL = "Email";
    public static final String LOGIN_NAME = "Login-Name";
    public static final String USER_ID = "User-Id";
    public static final String NAME = "Name";
    Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsoleUserExporter(UserExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public void export(List<User> users) throws AppException {
		switch(params.getWide()) {
		case standard:
			printStandard(users);
			break;
		case wide:
			printWide(users);
			break;
		case ultra:
			printUltra(users);
			break;
		}
	}

	private void printStandard(List<User> users) {
		Console.println(AsciiTable.getTable(borderStyle, users, Arrays.asList(
				new Column().header(USER_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getId),
				new Column().header(LOGIN_NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getLoginName),
				new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getName),
				new Column().header(EMAIL).with(User::getEmail),
				new Column().header(ENABLED).with(user -> Boolean.toString(user.isEnabled()))
				)));
	}

	private void printWide(List<User> users) {
		Console.println(AsciiTable.getTable(borderStyle, users, Arrays.asList(
				new Column().header(USER_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getId),
				new Column().header(LOGIN_NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getLoginName),
				new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getName),
				new Column().header(EMAIL).with(User::getEmail),
				new Column().header(ENABLED).with(user -> Boolean.toString(user.isEnabled())),
				new Column().header("Organization").with(user -> user.getOrganization().getName()),
				new Column().header("Role").with(User::getRole)
				)));
	}

	private void printUltra(List<User> users) {
		Console.println(AsciiTable.getTable(borderStyle, users, Arrays.asList(
				new Column().header(USER_ID).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getId),
				new Column().header(LOGIN_NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getLoginName),
				new Column().header(NAME).headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(User::getName),
				new Column().header(EMAIL).with(User::getEmail),
				new Column().header(ENABLED).with(user -> Boolean.toString(user.isEnabled())),
				new Column().header("Organization").with(user -> user.getOrganization().getName()),
				new Column().header("Role").with(User::getRole),
				new Column().header("Created on").with(user -> new Date(user.getCreatedOn()).toString()),
				//new Column().header("Last seen").with(user -> getLastSeen(user)), // Not supported by the REST-API - Only returned for the currentUser :-(
				new Column().header("Type").with(User::getType),
				new Column().header("State").with(User::getState)
				)));
	}

	@Override
	public UserFilter getFilter() {
		return getBaseFilterBuilder().build();
	}

}
