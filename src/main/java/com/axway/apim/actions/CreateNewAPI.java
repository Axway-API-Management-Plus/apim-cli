package com.axway.apim.actions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.CreateAPIProxy;
import com.axway.apim.actions.tasks.ImportBackendAPI;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;

public class CreateNewAPI {

	public void execute(APIChangeState changes) {
		
		Transaction context = Transaction.getInstance();
		context.beginTransaction();
		
		new ImportBackendAPI(changes.getDesiredAPI(), changes.getActualAPI()).execute();

		new CreateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		// As we have just created an API-Manager API, we should reflect this for further processing
		//((APIManagerAPI)changes.getActualAPI()).setApiConfiguration((JsonNode)context.get("lastResponse"));
		IAPIDefinition createdAPI = new APIManagerAPI((JsonNode)context.get("lastResponse"));
		changes.setIntransitAPI(createdAPI);
		// Force to initially update the API into the desired state!
		List<String> changedProps = getAllProps(changes.getDesiredAPI());
		// ... here we basically need to add all props to initially bring the API in sync!
		new UpdateAPIProxy(changes.getDesiredAPI(), createdAPI).execute(changedProps);
		
		// This is special, as the status is not a property and requires some additional actions!
		new UpdateAPIStatus(changes.getDesiredAPI(), createdAPI).execute();
	}
	
	/**
	 * @param desiredAPI
	 * @return
	 */
	private List<String> getAllProps(IAPIDefinition desiredAPI) {
		List<String> allProps = new Vector<String>();
		try {
			for (Field field : desiredAPI.getClass().getSuperclass().getDeclaredFields()) {
				if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
					String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method method = desiredAPI.getClass().getMethod(getterMethodName, null);
					Object desiredValue = method.invoke(desiredAPI, null);
					// For new APIs don't include empty properties (this including MissingNodes)
					if(desiredValue==null) continue; 
					allProps.add(field.getName());
				}
			}
			return allProps;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
