package plugin.application;

import org.testng.annotations.Test;

import com.axway.apim.plugins.application.ApplicationPlugin.ApplicationExtension;

public class ApplicationPluginTest {

	@Test
	public void testNoApplicationDefined() {
		ApplicationExtension applicationPlugin = new ApplicationExtension();
		
		//APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

	}

	@Test
	public void testAllApplicationsExists() {

	}

	@Test
	public void someApplicationsMissing() {

	}

}
