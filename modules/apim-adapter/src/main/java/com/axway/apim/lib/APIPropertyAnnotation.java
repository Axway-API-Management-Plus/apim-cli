package com.axway.apim.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface APIPropertyAnnotation {
	
	public boolean isBreaking() default false;
	
	/**
	 * @return true, if the property will be copied from the Desired to the Actual API. If false, the property is not copied.
	 */
	public boolean copyProp() default true;
	
	public String[] writableStates();
	
	public Class propHandler() default void.class;

}
