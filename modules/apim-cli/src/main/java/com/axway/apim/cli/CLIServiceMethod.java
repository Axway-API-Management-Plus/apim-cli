package com.axway.apim.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CLIServiceMethod {
	
	/**
	 * @return the description of the method
	 */
	public String description() default "";
	
	/**
	 * @return the name of the method if you don't want to use the original method name
	 */
	public String name() default "";
}
