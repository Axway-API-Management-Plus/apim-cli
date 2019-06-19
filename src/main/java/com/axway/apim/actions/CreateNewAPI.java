package com.axway.apim.actions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.CreateAPIProxy;
import com.axway.apim.actions.tasks.ImportBackendAPI;
import com.axway.apim.actions.tasks.ManageClientApps;
import com.axway.apim.actions.tasks.ManageClientOrgs;
import com.axway.apim.actions.tasks.UpdateAPIImage;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.actions.tasks.UpdateQuotaConfiguration;
import com.axway.apim.actions.tasks.UpgradeAccessToNewerAPI;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.IAPI;
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
		
		Transaction context = Transaction.getInstance();
		
		// Force to initially update the API into the desired state!
		List<String> changedProps = getAllProps(changes.getDesiredAPI());
		
		VhostPropertyHandler vHostHandler = new VhostPropertyHandler(changedProps);
		
		new ImportBackendAPI(changes.getDesiredAPI(), changes.getActualAPI()).execute();

		new CreateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		// As we have just created an API-Manager API, we should reflect this for further processing
		IAPI createdAPI = APIManagerAdapter.getInstance().getAPIManagerAPI((JsonNode)context.get("lastResponse"), changes.getDesiredAPI());
		changes.setIntransitAPI(createdAPI);
		
		// ... here we basically need to add all props to initially bring the API in sync!
		// But without updating the Swagger, as we have just imported it!
		new UpdateAPIProxy(changes.getDesiredAPI(), createdAPI).execute(changedProps);
		
		// If an image is included, update it
		if(changes.getDesiredAPI().getImage()!=null) {
			new UpdateAPIImage(changes.getDesiredAPI(), createdAPI).execute();
		}
		// This is special, as the status is not a normal property and requires some additional actions!
		new UpdateAPIStatus(changes.getDesiredAPI(), createdAPI).execute();
		
		if(reCreation) {
			// In case, the existing API is already in use (Published), we have to grant access to our new imported API
			new UpgradeAccessToNewerAPI(changes.getIntransitAPI(), changes.getActualAPI()).execute();
		}
		
		// Is a Quota is defined we must manage it
		new UpdateQuotaConfiguration(changes.getDesiredAPI(), createdAPI).execute();
		
		// Grant access to the API
		new ManageClientOrgs(changes.getDesiredAPI(), createdAPI).execute();
		
		// Handle subscription to applications
		new ManageClientApps(changes.getDesiredAPI(), createdAPI, changes.getActualAPI()).execute();
		
		// V-Host must be managed almost at the end, as the status must be set already to "published"
		vHostHandler.handleVHost(changes.getDesiredAPI(), createdAPI);
		
		APIPropertiesExport.getInstance().setProperty("feApiId", createdAPI.getApiId());
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
