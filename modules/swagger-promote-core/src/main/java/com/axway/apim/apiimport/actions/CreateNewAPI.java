package com.axway.apim.apiimport.actions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.api.API;
import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.ActualAPI;
import com.axway.apim.apiimport.actions.tasks.CreateAPIProxy;
import com.axway.apim.apiimport.actions.tasks.ImportBackendAPI;
import com.axway.apim.apiimport.actions.tasks.ManageClientApps;
import com.axway.apim.apiimport.actions.tasks.ManageClientOrgs;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIImage;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIProxy;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIStatus;
import com.axway.apim.apiimport.actions.tasks.UpdateQuotaConfiguration;
import com.axway.apim.apiimport.actions.tasks.UpgradeAccessToNewerAPI;
import com.axway.apim.apiimport.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.apiimport.state.APIChangeState;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.rollback.RollbackAPIProxy;
import com.axway.apim.lib.rollback.RollbackBackendAPI;
import com.axway.apim.lib.rollback.RollbackHandler;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class is used by the {@link APIManagerAdapter#applyChanges(APIChangeState)} to create a new API. 
 * It's called, when an existing API can't be found.
 * 
 * @author cwiechmann@axway.com
 */
public class CreateNewAPI {
	
	static Logger LOG = LoggerFactory.getLogger(CreateNewAPI.class);

	public void execute(APIChangeState changes, boolean reCreation) throws AppException {
		
		IAPI createdAPI = null;
		
		Transaction context = Transaction.getInstance();
		RollbackHandler rollback = RollbackHandler.getInstance();
		
		// During Re-Creation we have to Re-Init the Application-State 
		//if(reCreation) APIManagerAdapter.getInstance().setAllApps(null);
		
		// Force to initially update the API into the desired state!
		List<String> changedProps = getAllProps(changes.getDesiredAPI());
		
		VhostPropertyHandler vHostHandler = new VhostPropertyHandler(changedProps);
		new ImportBackendAPI(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		// Register the created BE-API to be rolled back in case of an error
		IAPI rollbackAPI = new APIBaseDefinition();
		((API)rollbackAPI).setName(changes.getDesiredAPI().getName());
		((API)rollbackAPI).setApiId((String)context.get("backendAPIId"));
		((APIBaseDefinition)rollbackAPI).setCreatedOn((String)context.get("backendAPICreatedOn"));
		rollback.addRollbackAction(new RollbackBackendAPI(rollbackAPI));
		
		try {
			new CreateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		} catch (Exception e) {
			rollback.addRollbackAction(new RollbackAPIProxy(rollbackAPI));
			throw e;
		} 
		rollback.addRollbackAction(new RollbackAPIProxy(rollbackAPI)); // In any case, register the API just created for a potential rollback
		
		try {
			// As we have just created an API-Manager API, we should reflect this for further processing
			createdAPI = APIManagerAdapter.getInstance().getAPIManagerAPI((JsonNode)context.get("lastResponse"), changes.getDesiredAPI(), ActualAPI.class);
			// Register the created FE-API to be rolled back in case of an error
			((API)rollbackAPI).setId(createdAPI.getId());
			changes.setIntransitAPI(createdAPI);
			
			// ... here we basically need to add all props to initially bring the API in sync!
			// But without updating the Swagger, as we have just imported it!
			new UpdateAPIProxy(changes.getDesiredAPI(), createdAPI).execute(changedProps);
			
			// If an image is included, update it
			if(changes.getDesiredAPI().getImage()!=null) {
				new UpdateAPIImage(changes.getDesiredAPI(), createdAPI).execute();
			}
			// This is special, as the status is not a normal property and requires some additional actions!
			UpdateAPIStatus statusUpdate = new UpdateAPIStatus(changes.getDesiredAPI(), createdAPI);
			statusUpdate.execute();
			((API)rollbackAPI).setState(createdAPI.getState());
			statusUpdate.updateRetirementDate(changes);
			
			if(reCreation && changes.getActualAPI().getState().equals(IAPI.STATE_PUBLISHED)) {
				// In case, the existing API is already in use (Published), we have to grant access to our new imported API
				new UpgradeAccessToNewerAPI(changes.getIntransitAPI(), changes.getActualAPI()).execute();
			}
			
			// Is a Quota is defined we must manage it
			new UpdateQuotaConfiguration(changes.getDesiredAPI(), createdAPI).execute();
			
			// Grant access to the API
			new ManageClientOrgs(changes.getDesiredAPI(), createdAPI).execute(reCreation);
			
			// Handle subscription to applications
			new ManageClientApps(changes.getDesiredAPI(), createdAPI, changes.getActualAPI()).execute(reCreation);
			
			// V-Host must be managed almost at the end, as the status must be set already to "published"
			vHostHandler.handleVHost(changes.getDesiredAPI(), createdAPI);
		} catch (Exception e) {
			throw e;
		} finally {
			if(createdAPI==null) {
				LOG.warn("Cant create PropertiesExport as createdAPI is null");
			} else {
				APIPropertiesExport.getInstance().setProperty("feApiId", createdAPI.getId());
			}
		}
	}
	
	/**
	 * @param desiredAPI
	 * @return
	 * @throws AppException 
	 */
	private List<String> getAllProps(IAPI desiredAPI) throws AppException {
		List<String> allProps = new Vector<String>();
		try {
			for (Field field : desiredAPI.getClass().getSuperclass().getDeclaredFields()) {
				if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
					String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method method = desiredAPI.getClass().getMethod(getterMethodName, null);
					Object desiredValue = method.invoke(desiredAPI, null);
					// For new APIs don't include empty properties (this includes MissingNodes)
					if(desiredValue==null) continue;
					// We have just inserted the Swagger-File
					if(field.getName().equals("apiDefinition")) continue;
					allProps.add(field.getName());
				}
			}
			return allProps;
		} catch (Exception e) {
			throw new AppException("Can't inspect properties to create new API!", ErrorCode.CANT_UPGRADE_API_ACCESS, e);
		}
	}
}
