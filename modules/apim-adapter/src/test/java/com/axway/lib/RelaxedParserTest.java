package com.axway.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.RelaxedParser;

public class RelaxedParserTest {
	@Test
	public void testParserWithNoArgOption() throws ParseException {
		String[] testArgs = new String[] {"-h", "api-env", "-wide", "-u", "apiadmin", "-p", "changeme", "-id", "5a711858-ab28-406e-92da-5a1d96f58668", "-ignoreCache"};
		CommandLineParser parser = new RelaxedParser();
		
		Options options = new Options();
		Option opt = Option.builder("h").hasArg().longOpt("host").argName("api-host").desc("The API-Manager hostname the API should be imported").build();
		options.addOption(opt);
		
		opt = Option.builder("u").hasArg().longOpt("username").argName("apiadmin").desc("Username used to authenticate. Please note, that this user must have Admin-Role").build();
		options.addOption(opt);
		
		opt = Option.builder("p").hasArg().longOpt("password").argName("changeme").desc("Password used to authenticate").build();
		options.addOption(opt);
		
		opt = new Option("wide", "A wider view of data to be returned by the export implementation. Requesting more data has a performance impact.");
		options.addOption(opt);
		
		opt = new Option("ignoreCache", "The cache for REST-API calls against the API-Manager isn't used at all.");
		options.addOption(opt);
		
		CommandLine cmd = parser.parse(options, testArgs);
		
		Assert.assertEquals(cmd.getOptionValue("host"), "api-env"); 
		Assert.assertEquals(cmd.getOptionValue("username"), "apiadmin");
		Assert.assertEquals(cmd.getOptionValue("password"), "changeme");
		Assert.assertTrue(cmd.hasOption("wide"));
		Assert.assertTrue(cmd.hasOption("ignoreCache"));
	}
	
	@Test
	public void testParserWithArgOptionsOnly() throws ParseException {
		String[] testArgs = new String[] {"-h", "api-env", "-u", "apiadmin", "-p", "changeme", "-id", "5a711858-ab28-406e-92da-5a1d96f58668"};
		CommandLineParser parser = new RelaxedParser();
		
		Options options = new Options();
		Option opt = Option.builder("h").hasArg().longOpt("host").argName("api-host").desc("The API-Manager hostname the API should be imported").build();
		options.addOption(opt);
		
		opt = Option.builder("u").hasArg().longOpt("username").argName("apiadmin").desc("Username used to authenticate. Please note, that this user must have Admin-Role").build();
		options.addOption(opt);
		
		opt = Option.builder("p").hasArg().longOpt("password").argName("changeme").desc("Password used to authenticate").build();
		options.addOption(opt);
		
		Option option = new Option("wide", "A wider view of data to be returned by the export implementation. Requesting more data has a performance impact.");
		options.addOption(option);
		
		CommandLine cmd = parser.parse(options, testArgs);
		
		Assert.assertEquals(cmd.getOptionValue("host"), "api-env"); 
		Assert.assertEquals(cmd.getOptionValue("username"), "apiadmin");
		Assert.assertEquals(cmd.getOptionValue("password"), "changeme");
		Assert.assertFalse(cmd.hasOption("wide"));
	}
}
