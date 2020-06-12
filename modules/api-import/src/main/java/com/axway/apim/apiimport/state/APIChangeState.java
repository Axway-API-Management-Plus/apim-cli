package com.axway.apim.apiimport.state;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.API;
import com.axway.apim.apiimport.actions.CreateNewAPI;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.CommandParameters;
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
	private API intransitAPI;
	private API desiredAPI;
	
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
	public APIChangeState(API actualAPI, API desiredAPI) throws AppException {
		super();
		this.actualAPI = actualAPI;
		this.desiredAPI = desiredAPI;
		if(actualAPI==null) { // No existing API found, just create a new one and that's all
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
		if(actualAPI==null) {
			return; //Nothing to do, as we don't have an existing API
		}
		if(!desiredAPI.getOrganization().equals(actualAPI.getOrganization()) && !CommandParameters.getInstance().changeOrganization()) {
			LOG.debug("You may set the toggle: changeOrganization=true to allow to changing the organization of an existing API.");
			ErrorState.getInstance().setError("The API you would like to register already exists for another organization.", ErrorCode.API_ALREADY_EXISTS, false);
			throw new AppException("The API you would like to register already exists for another organization.", ErrorCode.API_ALREADY_EXISTS);
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
	
	/**
	 * Copied all changed properties of the API having APIPropertyAnnotation set to copyProp = true (default)
	 * @throws AppException if something goes wrong
	 */
	public void copyChangedProps() throws AppException {
		Field field = null;
		
		if(getAllChanges().size()!=0) {
			String logMessage = "Updating Frontend-API (Proxy) for the following properties: ";
			for(String fieldName : getAllChanges()) {
				try {
					field = this.desiredAPI.getClass().getSuperclass().getDeclaredField(fieldName);
					APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
					if(!property.copyProp()) continue;
					if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
						String getterMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						Method getMethod = this.desiredAPI.getClass().getSuperclass().getMethod(getterMethodName, null);
						Object desiredObject = getMethod.invoke(this.desiredAPI, null);
						Method setMethod = this.actualAPI.getClass().getMethod(setterMethodName, desiredObject.getClass());
						
						setMethod.invoke(this.actualAPI, desiredObject);
						logMessage = logMessage + fieldName + " ";
					}
				} catch (Exception e) {
					throw new AppException("Can't handle property: "+fieldName+" to update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
				}
			}
			LOG.info(logMessage);
		} else {
			LOG.debug("API-Proxy requires no updates");
		}
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
	 * The IntransitAPI is used/set, when a new API has been created in API-Manager 
	 * while the "old actual API" still exists. This is required for instance when 
	 * the API must be Re-Created, before told old can be deleted.
	 * This API basically stores the <b>actual</b> API before the real old actual API 
	 * can be deleted.
	 * 
	 * @return the in TransitAPI.
	 * @see CreateNewAPI 
	 */
	public API getIntransitAPI() {
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
	public void setIntransitAPI(API intransitAPI) {
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
	
	public static API copyRequiredPropertisFromCreatedAPI(API desiredAPI, API createdAPI) throws AppException {
		desiredAPI.setId(createdAPI.getId());
		desiredAPI.setApiId(createdAPI.getApiId());
		desiredAPI.setActualState(createdAPI.getState()); // Copy the original state into a special field used during API-Proxy update
		desiredAPI.setCreatedBy(createdAPI.getCreatedBy());
		desiredAPI.setCreatedOn(createdAPI.getCreatedOn());
		if(desiredAPI.getOutboundProfiles()==null) desiredAPI.setOutboundProfiles(createdAPI.getOutboundProfiles());
		if(desiredAPI.getInboundProfiles()==null) desiredAPI.setInboundProfiles(createdAPI.getInboundProfiles());
		if(desiredAPI.getServiceProfiles()==null) desiredAPI.setServiceProfiles(createdAPI.getServiceProfiles());
		if(desiredAPI.getSecurityProfiles()==null) desiredAPI.setSecurityProfiles(createdAPI.getSecurityProfiles());
		if(desiredAPI.getAuthenticationProfiles()==null) desiredAPI.setAuthenticationProfiles(createdAPI.getAuthenticationProfiles());
		if(desiredAPI.getCaCerts()==null) desiredAPI.setCaCerts(createdAPI.getCaCerts());
		return desiredAPI;
	}
}
