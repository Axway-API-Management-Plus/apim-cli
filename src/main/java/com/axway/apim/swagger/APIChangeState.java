package com.axway.apim.swagger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;

/**
 * @author cwiechmann
 * This class is key, as the desired and actual API comes together and 
 * here we basically control how to apply the desired state into the API-Manager. 
 * 
 * This class iterates through all the declared API-Properties, in case of changes 
 * the belonging APIManagerAction is queued to be executed.
 * This way of working is esp. important when updating an existing API, without 
 * replacing it. 
 */
public class APIChangeState {
	
	private static Logger LOG = LoggerFactory.getLogger(APIChangeState.class);
	
	private IAPIDefinition actualAPI;
	private IAPIDefinition desiredAPI;
	
	private boolean isBreaking = false;
	private boolean updateExistingAPI = true;
	
	private List<String> breakingChanges = new Vector<String>();
	private List<String> nonBreakingChanges = new Vector<String>();

	public APIChangeState(IAPIDefinition actualAPI, IAPIDefinition desiredAPI) {
		super();
		this.actualAPI = actualAPI;
		this.desiredAPI = desiredAPI;
		if(!actualAPI.isValid()) { // No existing API found, just create a new one and that's all
			LOG.debug("No existing API found. Creating  complete new API");
			return;
		}
		getChanges();
	}
	
	/**
	 * This method is reading all @APIDefinition annotations to verify which API-Property will 
	 * result in a breaking change. For that, it calls the equals methods on each property 
	 * and if not equals it turns into a breaking change.
	 * Additionally, we need to know, if changes can be applied on the existing API. For that 
	 * we need to check, if all properties can be are writable, which is depending on the state of the 
	 * actual API. 
	 */
	private void getChanges() {
		if(!actualAPI.isValid()) {
			return; //Nothing to do, as we don't have an existing API
		}
		try {
			for (Field field : desiredAPI.getClass().getSuperclass().getDeclaredFields()) {
				if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
					String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method method = desiredAPI.getClass().getMethod(getterMethodName, null);
					Method method2 = actualAPI.getClass().getMethod(getterMethodName, null);
					Object desiredValue = method.invoke(desiredAPI, null);
					Object actualValue = method2.invoke(actualAPI, null);
					if(!desiredValue.equals(actualValue)) {
						APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
						if (property.isBreaking()) {
							this.isBreaking = true;
							this.breakingChanges.add(field.getName());
						} else {
							this.nonBreakingChanges.add(field.getName());
						}
						if (!isWritable(property, this.actualAPI.getStatus())) {
							this.updateExistingAPI = false; // Found a NON-Changeable property, can't update the existing API
						}
					}

				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public IAPIDefinition getActualAPI() {
		return actualAPI;
	}

	public void setActualAPI(IAPIDefinition actualAPI) {
		this.actualAPI = actualAPI;
	}

	public IAPIDefinition getDesiredAPI() {
		return desiredAPI;
	}

	public void setDesiredAPI(IAPIDefinition desiredAPI) {
		this.desiredAPI = desiredAPI;
	}
	
	public boolean hasAnyChanges() {
		if(this.breakingChanges.size()==0 && this.nonBreakingChanges.size()==0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isBreaking() {
		return isBreaking;
	}

	public boolean isUpdateExistingAPI() {
		return updateExistingAPI;
	}

	public List<String> getBreakingChanges() {
		return breakingChanges;
	}

	public List<String> getNonBreakingChanges() {
		return nonBreakingChanges;
	}
	
	private static boolean isWritable(APIPropertyAnnotation property, String actualStatus) {
		// Get the field annotation via reflection
		// Check, if the actualState is in the writableStates
		try {
			String[] writableStates = property.writableStates();
			for(String status : writableStates) {
				if (actualStatus.equals(status)) {
					return true;
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
