package com.axway.apim.changeAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.APIExportApp;
import com.axway.apim.lib.CoreParameters;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class ChangeTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(ChangeTestAction.class);
	
	File testDir = null;
	
	@Override
	public void doExecute(TestContext context) {
		String stage				= null;
		boolean useEnvironmentOnly = false;
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};
		LOG.info("API-Manager import is using user: '"+context.replaceDynamicContentInString("${oadminUsername1}")+"'");
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		try {
			useEnvironmentOnly 	= Boolean.parseBoolean(context.getVariable("useEnvironmentOnly"));
		} catch (Exception ignore) {};
		
		boolean enforce = false;
		boolean ignoreQuotas = false;
		boolean ignoreAdminAccount = false;
		boolean ignoreCache = false;
		String allowOrgAdminsToPublish = "true";
		boolean changeOrganization = false;
		String clientOrgsMode = CoreParameters.MODE_ADD;
		String clientAppsMode = CoreParameters.MODE_ADD;
		String quotaMode = CoreParameters.MODE_ADD;
		
		String newBackend = null;
		String oldBackend = null;
		String name = null;
		
		try {
			enforce = Boolean.parseBoolean(context.getVariable("enforce"));
		} catch (Exception ignore) {};
		try {
			ignoreQuotas = Boolean.parseBoolean(context.getVariable("ignoreQuotas"));
		} catch (Exception ignore) {};
		try {
			quotaMode = context.getVariable("quotaMode");
		} catch (Exception ignore) {};
		try {
			clientOrgsMode = context.getVariable("clientOrgsMode");
		} catch (Exception ignore) {};
		try {
			clientAppsMode = context.getVariable("clientAppsMode");
		} catch (Exception ignore) {};
		try {
			ignoreAdminAccount = Boolean.parseBoolean(context.getVariable("ignoreAdminAccount"));
		} catch (Exception ignore) {};
		try {
			allowOrgAdminsToPublish = context.getVariable("allowOrgAdminsToPublish");
		} catch (Exception ignore) {};
		try {
			changeOrganization = Boolean.parseBoolean(context.getVariable("changeOrganization"));
		} catch (Exception ignore) {};
		try {
			ignoreCache = Boolean.parseBoolean(context.getVariable("ignoreCache"));
		} catch (Exception ignore) {};
		try {
			name = context.getVariable("name");
		} catch (Exception ignore) {};
		try {
			newBackend = context.getVariable("newBackend");
		} catch (Exception ignore) {};
		try {
			oldBackend = context.getVariable("oldBackend");
		} catch (Exception ignore) {};
		
		
		if(stage==null) {
			stage = "NOT_SET";
		}
		
		List<String> args = new ArrayList<String>();
		if(useEnvironmentOnly) {
			args.add("-s");
			args.add(stage);
		} else {
			args.add("-h");
			args.add(context.replaceDynamicContentInString("${apiManagerHost}"));
			args.add("-u");
			args.add(context.replaceDynamicContentInString("${oadminUsername1}"));
			args.add("-p");
			args.add(context.replaceDynamicContentInString("${oadminPassword1}"));
			args.add("-s");
			args.add(stage);
			args.add("-quotaMode");
			args.add(quotaMode);
			args.add("-clientOrgsMode");
			args.add(clientOrgsMode);
			args.add("-clientAppsMode");
			args.add(clientAppsMode);
			args.add("-allowOrgAdminsToPublish");
			args.add(allowOrgAdminsToPublish);
			if(newBackend!=null) {
				args.add("-newBackend");
				args.add(newBackend);
			}
			if(name!=null) {
				args.add("-name");
				args.add(name);
			}
			if(oldBackend!=null) {
				args.add("-oldBackend");
				args.add(oldBackend);
			}
			if(changeOrganization)	args.add("-changeOrganization");
			if(enforce)				args.add("-force");
			if(ignoreQuotas) 		args.add("-ignoreQuotas");
			if(ignoreCache) 		args.add("-ignoreCache");
			if(ignoreAdminAccount) 	args.add("-ignoreAdminAccount");
		}
		LOG.info(args.toString());
		int rc = APIExportApp.change(args.toArray(new String[args.size()]));
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
}
