package com.axway.apim.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface APIManagerAlertsAnnotation {
	
	enum AlertType {
		Application("Application"),
		ApplicationAPIAccess("Application - API access"),
		ApplicationCredentials("Application Credentials"),
		ApplicationDeveloper("Application developer"),
		Organization("Organization"),
		OrganizationAPIAccess("Organization - API access"), 
		APIRegistration("API Registration"), 
		APICatalog("API Catalog"),
		Quota("Quota");
		
		private String clearName;
		
		AlertType(String clearName) {
			this.clearName = clearName;
		}

		public String getClearName() {
			return clearName;
		}
	}
	
	AlertType alertType();

	String description() default "";
	
	String name();
}
