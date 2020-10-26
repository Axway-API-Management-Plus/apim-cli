package com.axway.apim.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface APIManagerConfigAnnotation {
	
	public enum ConfigType {
		APIManager("API Manager settings"),
		APIPortal("API Portal settings"),
		General("General settings"),
		Session("Password, Login & Session Management settings"),
		Delegation("Organization Administrator Delegation"),
		APIRegistration("API Registration"), 
		APIImport("API Import"), 
		GlobalPolicies("Global Policies"),
		FaultHandlers("Fault Handlers"), 
		AdvisoryBanner("Advisory Banner");
		
		private String clearName;
		
		ConfigType(String clearName) {
			this.clearName = clearName;
		}

		public String getClearName() {
			return clearName;
		}
	}
	
	public ConfigType configType();

	public String description();
	
	public String name();
}
