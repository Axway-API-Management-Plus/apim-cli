package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Alerts;
import com.axway.apim.lib.APIManagerAlertsAnnotation;
import com.axway.apim.lib.APIManagerAlertsAnnotation.AlertType;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;

import java.lang.reflect.Field;

public class ConsolePrinterAlerts {

	APIManagerAdapter adapter;

	AlertType[] alertsTypes = new AlertType[] {
			AlertType.Application,
			AlertType.ApplicationAPIAccess,
			AlertType.ApplicationCredentials,
			AlertType.ApplicationDeveloper,
			AlertType.Organization,
			AlertType.OrganizationAPIAccess,
			AlertType.APIRegistration,
			AlertType.APICatalog,
			AlertType.Quota
	};

	public ConsolePrinterAlerts() throws AppException {
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new AppException("Unable to get APIManagerAdapter", ErrorCode.UNXPECTED_ERROR);
		}
	}

	public void export(Alerts alerts) throws AppException {
		Console.println();
		Console.println("Alerts for: '" + APIManagerAdapter.getInstance().getApiManagerName() + "' Version: " + APIManagerAdapter.getInstance().getApiManagerVersion());
		Console.println();
		print(alerts, alertsTypes);
	}

	private void print(Alerts alerts, AlertType[] alertTypes) {
		for(AlertType type : alertTypes) {
			Console.println(type.getClearName()+":");
			Field[] fields = Alerts.class.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(APIManagerAlertsAnnotation.class)) {
					APIManagerAlertsAnnotation annotation = field.getAnnotation(APIManagerAlertsAnnotation.class);
					if(annotation.alertType()==type) {
						String dots = ".....................................";
						Console.printf("%s %s: %s", annotation.name() , dots.substring(annotation.name().length()), Utils.getFieldValue(field.getName(), alerts));
					}
				}
			}
			Console.println();
		}
	}
}
