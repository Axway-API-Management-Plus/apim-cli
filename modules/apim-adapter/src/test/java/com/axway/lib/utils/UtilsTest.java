package com.axway.lib.utils;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.utils.Utils;

public class UtilsTest {
	
	String testConfig = this.getClass().getResource("/stageConfigTest/test-api-config.json").getFile();
	String stageConfig = this.getClass().getResource("/stageConfigTest/my-stage-test-api-config.json").getFile();
	
	@Test
	public void testGetStageConfigNoStage() {
		File stageConfigFile = Utils.getStageConfig(null, null, null);
		Assert.assertNull(stageConfigFile);
	}
	
	@Test
	public void testGetStageConfigSomeStage() {
		File stageConfigFile = Utils.getStageConfig("someStage", null, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "test-api-config.someStage.json");
	}
	
	@Test
	public void testGetStageConfigUnknownStage() {
		File stageConfigFile = Utils.getStageConfig("unknownStage", null, new File(testConfig));
		Assert.assertNull(stageConfigFile);
	}
	
	@Test
	public void testGetStageConfigFile() {
		File stageConfigFile = Utils.getStageConfig(null, stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndConfig() {
		File stageConfigFile = Utils.getStageConfig("prod", stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndRelativeConfig() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testMissingMandatoryCustomProperty() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
}
