package com.axway.apim.swagger.api;

import java.lang.reflect.Field;

import com.axway.apim.lib.APIPropertyAnnotation;

public class APIProperty {
	private boolean isChangeable;
	
	private boolean isBreaking;
	
	private boolean isNonBreaking;
	
	private String propertyName;
	
	private Object value;
	
	private Class actionClass;

	public APIProperty(boolean isChangeable, boolean isBreaking, boolean isNonBreaking, String propertyName,
			Object value) {
		super();
		this.isChangeable = isChangeable;
		this.isBreaking = isBreaking;
		this.isNonBreaking = isNonBreaking;
		this.propertyName = propertyName;
		this.value = value;
	}

	public boolean isChangeable() {
		return isChangeable;
	}

	public boolean isBreaking() {
		return isBreaking;
	}

	public boolean isNonBreaking() {
		return isNonBreaking;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object that) {
		if(that instanceof APIProperty) return false;
		APIProperty other = (APIProperty)that;
		if(this.getValue().equals(other.getValue())) return true;
		return false;
	}
	
	public static boolean isWritable(String propertyName, String actualStatus) {
		// Get the field annotation via reflection
		// Check, if the actualState is in the writableStates
		try {
			Field field = AbstractAPIDefinition.class.getDeclaredField(propertyName);
			if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
				APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
				String[] writableStates = property.writableStates();
				for(String status : writableStates) {
					if (actualStatus.equals(status)) {
						return true;
					}
				}
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
