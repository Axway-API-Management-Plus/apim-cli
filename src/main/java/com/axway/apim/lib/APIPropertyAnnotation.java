package com.axway.apim.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface APIPropertyAnnotation {
	
	public boolean isBreaking() default false;
	
	public String[] writableStates();
	
	public Class propHandler() default void.class;

}
