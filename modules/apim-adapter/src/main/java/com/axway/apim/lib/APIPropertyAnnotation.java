package com.axway.apim.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface APIPropertyAnnotation {

	/**
	 * Controls if an API-Property would potentially lead to a breaking change.
	 * @return true, if the API property can break client applications.
	 */
	boolean isBreaking() default false;

	/**
	 * If set to true, a change to this API-Property will force the API to be re-created.
	 * For instance, when the OpenAPI/Swagger spec is changed.
	 * @return true if a change to this API-Property must re-create the API. Defaults to false
	 */
	boolean isRecreate() default false;

	/**
	 * This property should be set to false for all properties, not managed directly by the API-Proxy endpoint. For instance
	 * clientOrganization, applications, etc.
	 * This properties should stay in the DesiredAPI and not copied into the Actual-API to make decisions possible, about
	 * what to do (for instance which organizations to remove or add)
	 * @return true, if the property will be copied from the Desired to the Actual API. If false, the property is not copied.
	 */
	boolean copyProp() default true;

	/**
	 * @return an Array of states this API property can be changed
	 */
	String[] writableStates();

    /**
     * Be default ignore null values
     * @return true
     */
    boolean ignoreNull() default true;
}
