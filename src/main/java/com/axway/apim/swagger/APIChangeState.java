package com.axway.apim.swagger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.state.IAPI;

/**
 * This class is key, as the desired and actual API comes together.
 * 
 * This class compares the desired- with the actual-API to create the Change-State. Basically 
 * a list of changes that are needed to bring the API in sync.
 * 
 * For that the class iterates through all the declared API-Properties, in case of changes 
 * the belonging APIManagerAction is queued to be executed.
 * This way of working is esp. important when updating an existing API, without 
 * replacing it. 
 * 
 * @author cwiechmann
 */
public class APIChangeState {
	
	private static Logger LOG = LoggerFactory.getLogger(APIChangeState.class);
	
	private IAPI actualAPI;
	private IAPI intransitAPI;
	private IAPI desiredAPI;
	
	private boolean isBreaking = false;
	private boolean updateExistingAPI = true;
	
	private List<String> breakingChanges = new Vector<String>();
	private List<String> nonBreakingChanges = new Vector<String>();

	/**
	 * Constructs the APIChangeState based on the given Actual- and Desired-API.
	 * @param actualAPI - The API taken from the API-Manager
	 * @param desiredAPI - The API loaded from the Swagger + Config
	 * @throws AppException - Is thrown when something goes wrong.
	 */
	public APIChangeState(IAPI actualAPI, IAPI desiredAPI) throws AppException {
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
	 * @throws AppException 
	 */
	private void getChanges() throws AppException {
		if(!actualAPI.isValid()) {
			return; //Nothing to do, as we don't have an existing API
		}
		for (Field field : desiredAPI.getClass().getSuperclass().getDeclaredFields()) {
			try {
				if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
					String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method method = desiredAPI.getClass().getMethod(getterMethodName, null);
					Method method2 = actualAPI.getClass().getMethod(getterMethodName, null);
					Object desiredValue = method.invoke(desiredAPI, null);
					Object actualValue = method2.invoke(actualAPI, null);
					if(desiredValue == null && actualValue == null) continue;
					if(desiredValue == null) {
						LOG.debug("Ignoring Null-Property: " + field.getName() + "[Desired: '"+desiredValue+"' vs Actual: '"+actualValue+"']");
						continue; // No change, if nothing is provided!
					}
					// desiredValue == null - This can be used to reset/clean a property! (Need to think about this!)
					if((desiredValue!=null && actualValue==null) || !(compareValues(actualValue, desiredValue))) {
						APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
						if (property.isBreaking()) {
							this.isBreaking = true;
							this.breakingChanges.add(field.getName());
						} else {
							this.nonBreakingChanges.add(field.getName());
						}
						if (!isWritable(property, this.actualAPI.getState())) {
							this.updateExistingAPI = false; // Found a NON-Changeable property, can't update the existing API
						}
						LOG.info("Changed property: " + field.getName() + "[Desired: '"+desiredValue+"' vs Actual: '"+actualValue+"']");
					} else {
						LOG.debug("No change for property: " + field.getName() + "[Desired: '"+desiredValue+"' vs Actual: '"+actualValue+"']");
					}

				}
			} catch (Exception e) {
				throw new AppException("Can't verify API-Change-State for: " + field.getName(), ErrorCode.CANT_CREATE_STATE_CHANGE, e);
			}
		}
	}

	/**
	 * @return the API-Manager API that has been given to this APIChangeState instance
	 */
	public IAPI getActualAPI() {
		return actualAPI;
	}

	/**
	 * @param actualAPI overwrites the API-Manager API instance
	 */
	public void setActualAPI(IAPI actualAPI) {
		this.actualAPI = actualAPI;
	}

	/**
	 * @return the desired API that has been given to this APIChangeState instance
	 */
	public IAPI getDesiredAPI() {
		return desiredAPI;
	}

	/**
	 * @param desiredAPI overwrites the desired API.
	 */
	public void setDesiredAPI(IAPI desiredAPI) {
		this.desiredAPI = desiredAPI;
	}
	
	/**
	 * The IntransitAPI is used/set, when a new API has been created in API-Manager 
	 * while the "old actual API" still exists. This is required for instance when 
	 * the API must be Re-Created, before told old can be deleted.
	 * This API basically stores the <b>actual</b> API before the real old actual API 
	 * can be deleted.
	 * 
	 * @return the in TransitAPI.
	 * @see CreateNewAPI 
	 */
	public IAPI getIntransitAPI() {
		return intransitAPI;
	}

	/**
	 * The IntransitAPI is used/set, when a new API has been created in API-Manager 
	 * while the "old actual API" still exists. This is required for instance when 
	 * the API must be Re-Created, before told old can be deleted.
	 * This API basically stores the <b>actual</b> API before the real old actual API 
	 * can be deleted.
	 * 
	 * @param intransitAPI the intermediate API
	 * @see CreateNewAPI 
	 */
	public void setIntransitAPI(IAPI intransitAPI) {
		this.intransitAPI = intransitAPI;
	}

	/**
	 * @return true, if a breakingChange or a nonBreakingChange was found otherwise false.
	 */
	public boolean hasAnyChanges() {
		if(this.breakingChanges.size()==0 && this.nonBreakingChanges.size()==0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return true, if a Breaking-Change propery is found on an "Unpublished" API otherwise false.
	 * @throws AppException if the actualAPI.state can't be read 
	 */
	public boolean isBreaking() throws AppException {
		// We will only break API, if the API is no longer in state: "unpublished"
		if(this.actualAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) return false;
		return isBreaking;
	}

	/**
	 * @return true if all changes are writable to actual/current API-State.
	 */
	public boolean isUpdateExistingAPI() {
		return updateExistingAPI;
	}

	/**
	 * @return list of breaking changes
	 */
	public List<String> getBreakingChanges() {
		return breakingChanges;
	}

	/**
	 * @return list of Non-Breaking-Changes.
	 */
	public List<String> getNonBreakingChanges() {
		return nonBreakingChanges;
	}
	
	/**
	 * Helper method to check if a certain property can be updated in the current/actual API-State.
	 * @param property to be updated
	 * @param actualStatus the actual state of the API
	 * @return true if the property can be updated otherwise false
	 */
	private static boolean isWritable(APIPropertyAnnotation property, String actualStatus) {
		String[] writableStates = property.writableStates();
		for(String status : writableStates) {
			if (actualStatus.equals(status)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean compareValues(Object actualValue, Object desiredValue) {
		if(actualValue instanceof List) {
			return ((List<?>)actualValue).size() == ((List<?>)desiredValue).size() && 
					((List<?>)actualValue).containsAll((List<?>)desiredValue) && 
					((List<?>)desiredValue).containsAll((List<?>)actualValue);
		} else {
			return actualValue.equals(desiredValue);
		}
	}
}
