package com.axway.apim.apiimport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.lib.APIImportParams;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

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
	
	private API actualAPI;
	private API desiredAPI;
	
	private boolean isBreaking = false;
	private boolean updateExistingAPI = true;
	private boolean recreateAPI = false;
	
	private boolean proxyUpdateRequired = false;
	
	private List<String> breakingChanges = new Vector<String>();
	private List<String> nonBreakingChanges = new Vector<String>();
	
	private String isAdminAccountNeeded = null;

	/**
	 * Constructs the APIChangeState based on the given Actual- and Desired-API.
	 * @param actualAPI - The API taken from the API-Manager
	 * @param desiredAPI - The API loaded from the Swagger + Config
	 * @throws AppException - Is thrown when something goes wrong.
	 */
	public APIChangeState(API actualAPI, API desiredAPI) throws AppException {
		super();
		this.actualAPI = actualAPI;
		this.desiredAPI = desiredAPI;
		if(actualAPI==null) { // No existing API found, just create a new one and that's all
			LOG.debug("No existing API found. Creating  complete new API");
			return;
		}
		getChanges();
		isAdminAccountNeeded();
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
		if(actualAPI==null) {
			return; //Nothing to do, as we don't have an existing API
		}
		if(!desiredAPI.getOrganization().equals(actualAPI.getOrganization()) && !APIImportParams.getInstance().isChangeOrganization()) {
			LOG.debug("You may set the toggle: changeOrganization=true to allow to changing the organization of an existing API.");
			ErrorState.getInstance().setError("The API you would like to register already exists for another organization.", ErrorCode.API_ALREADY_EXISTS, false);
			throw new AppException("The API you would like to register already exists for another organization.", ErrorCode.API_ALREADY_EXISTS);
		}
		Field[] fields = (desiredAPI.getClass().equals(API.class)) ? desiredAPI.getClass().getDeclaredFields() :  desiredAPI.getClass().getSuperclass().getDeclaredFields();
		for (Field field : fields) {
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
						// Property change requires proxy update
						if(property.copyProp()) this.proxyUpdateRequired = true;
						if(property.isBreaking()) {
							this.isBreaking = true;
							this.breakingChanges.add(field.getName());
						} else {
							this.nonBreakingChanges.add(field.getName());
						}
						if(property.isRecreate()) {
							this.recreateAPI = true;
						}
						if (!isWritable(property, this.actualAPI.getState())) {
							this.updateExistingAPI = false; // Found a NON-Changeable property, can't update the existing API
						}
						LOG.debug("Changed property: " + field.getName() + "[Desired: '"+desiredValue+"' vs Actual: '"+actualValue+"']");
					} else {
						LOG.debug("No change for property: " + field.getName() + "[Desired: '"+desiredValue+"' vs Actual: '"+actualValue+"']");
					}

				}
			} catch (Exception e) {
				throw new AppException("Can't verify API-Change-State for: " + field.getName(), ErrorCode.CANT_CREATE_STATE_CHANGE, e);
			}
		}
	}
	
	public static void initCreatedAPI(API desiredAPI, API createdAPI) throws AppException {
		List<String> allProps = getAllAPIProperties();
		copyProps(desiredAPI, createdAPI, allProps, false);
	}
	
	public static void copyChangedProps(API desiredAPI, API createdAPI, List<String> changes) throws AppException {
		copyProps(desiredAPI, createdAPI, changes, true);
	}
	
	/**
	 * Copied all changed properties of the API having APIPropertyAnnotation set to copyProp = true (default)
	 * @param sourceAPI copy the properties from this source API
	 * @param targetAPI copy the properties into this target API
	 * @param propsToCopy the list of API properties to copy
	 * @param logMessage controls if a log message is created which properties are copied
	 * @throws AppException if something goes wrong
	 */
	public static void copyProps(API sourceAPI, API targetAPI, List<String> propsToCopy, boolean logMessage) throws AppException {
		Field field = null;
		
		Class clazz = (sourceAPI.getClass().equals(API.class)) ? sourceAPI.getClass() :  sourceAPI.getClass().getSuperclass();
		if(propsToCopy.size()!=0) {
			String message = "Updating Frontend-API (Proxy) for the following properties: ";
			for(String fieldName : propsToCopy) {
				try {
					field = clazz.getDeclaredField(fieldName);
					
					APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
					if(!property.copyProp()) continue;
					if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
						String getterMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						Method getMethod = clazz.getMethod(getterMethodName, null);
						Object desiredObject = getMethod.invoke(sourceAPI, null);
						if(desiredObject==null) continue;
						Method setMethod = targetAPI.getClass().getMethod(setterMethodName, field.getType());
						
						setMethod.invoke(targetAPI, desiredObject);
						message = message + fieldName + " ";
					}
				} catch (Exception e) {
					throw new AppException("Can't handle property: "+fieldName+" to update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
				}
			}
			if(logMessage) {
				LOG.info(message);
			}	
		} else {
			LOG.debug("API-Proxy requires no updates");
		}
	}
	
	private static List<String> getAllAPIProperties() {
		List<String> allAPIProps = new ArrayList<String>();
		Field[] fields = API.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {	
				allAPIProps.add(field.getName());
			}
		}
		return allAPIProps;
	}

	/**
	 * @return the API-Manager API that has been given to this APIChangeState instance
	 */
	public API getActualAPI() {
		return actualAPI;
	}

	/**
	 * @param actualAPI overwrites the API-Manager API instance
	 */
	public void setActualAPI(API actualAPI) {
		this.actualAPI = actualAPI;
	}

	/**
	 * @return the desired API that has been given to this APIChangeState instance
	 */
	public API getDesiredAPI() {
		return desiredAPI;
	}

	/**
	 * @param desiredAPI overwrites the desired API.
	 */
	public void setDesiredAPI(API desiredAPI) {
		this.desiredAPI = desiredAPI;
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
		if(this.actualAPI.getState().equals(API.STATE_UNPUBLISHED)) return false;
		if(this.actualAPI.getState().equals(API.STATE_PENDING)) return false;
		return isBreaking;
	}

	/**
	 * @return true if all changes are writable to actual/current API-State.
	 */
	public boolean isUpdateExistingAPI() {
		return updateExistingAPI;
	}

	/**
	 * @return true, if the API needs to be re-created (e.g. the API-Definition has changed)
	 */
	public boolean isRecreateAPI() {
		return recreateAPI;
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
	
	public boolean isProxyUpdateRequired() {
		return proxyUpdateRequired;
	}

	/**
	 * @return list of all changes.
	 */
	public List<String> getAllChanges() {
		List<String> allChanges = new ArrayList<String>();
		allChanges.addAll(nonBreakingChanges);
		allChanges.addAll(breakingChanges);
		return allChanges;
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
	
	public boolean isAdminAccountNeeded() throws AppException {
		// Initially set, right after comparing the Desired- with Actual-state
		if(this.isAdminAccountNeeded!=null) return Boolean.parseBoolean(this.isAdminAccountNeeded);
		// If the desired & actual API is state Unpublished - No Admin-Account is needed
		if((getDesiredAPI().getState().equals(API.STATE_UNPUBLISHED) || getDesiredAPI().getState().equals(API.STATE_DELETED)) && 
				(getActualAPI()==null || getActualAPI().getState().equals(API.STATE_UNPUBLISHED))) {
			this.isAdminAccountNeeded = "false";
		} else {
			this.isAdminAccountNeeded = "true";
		}
		return Boolean.parseBoolean(this.isAdminAccountNeeded);
	}
	
	public String waiting4Approval() throws AppException {
		String isWaitingMsg = "";
		if(isAdminAccountNeeded() && !APIManagerAdapter.hasAdminAccount()) {
			isWaitingMsg = "Waiting for approval ... ";
		}
		return isWaitingMsg;
	}
}
