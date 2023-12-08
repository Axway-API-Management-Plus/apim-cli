package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Alerts;
import com.axway.apim.lib.APIManagerAlertsAnnotation;
import com.axway.apim.lib.APIManagerAlertsAnnotation.AlertType;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ConsolePrinterAlerts {

	private static final Logger LOG = LoggerFactory.getLogger(ConsolePrinterAlerts.class);

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
						Console.printf("%s %s: %s", annotation.name() , dots.substring(annotation.name().length()), getFieldValue(field.getName(), alerts));
					}
				}
			}
			Console.println();
		}
	}

	private String getFieldValue(String fieldName, Alerts alerts) {
		try {
			PropertyDescriptor pd = new PropertyDescriptor(fieldName, alerts.getClass());
			Method getter = pd.getReadMethod();
			Object value = getter.invoke(alerts);
			return (value==null) ? "N/A" : value.toString();
		} catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				LOG.error(e.getMessage(), e);
			}
			return "Err";
		}
	}
}
