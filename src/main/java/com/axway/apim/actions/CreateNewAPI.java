package com.axway.apim.actions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import com.axway.apim.actions.rest.RestAPICall;
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

public class CreateNewAPI extends AbstractCommandHandler {

	@Override
	public void execute(APIChangeState changes) {
		
		Transaction context = Transaction.getInstance();
		context.beginTransaction();
		
		RestAPICall call = ImportBackendAPI.execute(changes.getDesiredAPI(), changes.getActualAPI());
		super.executeAPICall(call);
		call = CreateAPIProxy.execute(changes.getDesiredAPI(), changes.getActualAPI());
		super.executeAPICall(call);
		// As we have just created an API-Manager API, we should reflect this for further processing
		((APIManagerAPI)changes.getActualAPI()).setApiConfiguration((JsonNode)context.get("lastResponse"));
		// Force to initially update the API into the desired state!
		List<String> changedProps = getAllProps(changes.getDesiredAPI());
		// ... here we basically need to add all props to initially bring the API in sync!
		call = UpdateAPIProxy.execute(changes.getDesiredAPI(), changes.getActualAPI(), changedProps);
		super.executeAPICall(call);
		// This is special, as the status is not a property and requires some additional actions!
		call = UpdateAPIStatus.execute(changes.getDesiredAPI(), changes.getActualAPI());
		super.executeAPICall(call);
	}
	
	private List<String> getAllProps(IAPIDefinition desiredAPI) {
		List<String> allProps = new Vector<String>();
		try {
			for (Field field : desiredAPI.getClass().getSuperclass().getDeclaredFields()) {
				if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
					APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
					if (property.isBreaking()) {
						allProps.add(field.getName());
					}

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
		}
	}
}
