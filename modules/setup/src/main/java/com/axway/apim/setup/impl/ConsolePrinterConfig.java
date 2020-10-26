package com.axway.apim.setup.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.APIManagerConfigAnnotation;
import com.axway.apim.lib.APIManagerConfigAnnotation.ConfigType;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class ConsolePrinterConfig {
	
	protected static Logger LOG = LoggerFactory.getLogger(ConsolePrinterConfig.class);
	
	APIManagerAdapter adapter;
	
	StandardExportParams params;
	
	private String dots = ".....................................";
	
	ConfigType[] standardFields = new ConfigType[] {
			ConfigType.APIManager, 
			ConfigType.APIPortal, 
			ConfigType.General, 
			ConfigType.APIRegistration
	};
	ConfigType[] wideFields = new ConfigType[] {
			ConfigType.APIManager, 
			ConfigType.APIPortal, 
			ConfigType.General, 
			ConfigType.APIRegistration,
			ConfigType.APIImport,
			ConfigType.Delegation, 
			ConfigType.GlobalPolicies, 
			ConfigType.FaultHandlers
	};
	
	ConfigType[] ultraFields = new ConfigType[] {
			ConfigType.APIManager, 
			ConfigType.APIPortal, 
			ConfigType.General, 
			ConfigType.APIRegistration, 
			ConfigType.APIImport, 
			ConfigType.Delegation, 
			ConfigType.GlobalPolicies, 
			ConfigType.FaultHandlers, 
			ConfigType.Session, 
			ConfigType.AdvisoryBanner, 
	};

	public ConsolePrinterConfig(StandardExportParams params) {
		this.params = params;
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}

	public void export(Config config) throws AppException {
		System.out.println();
		System.out.println("Configuration for: '" + config.getPortalName() + "' Version: " + config.getProductVersion());
		System.out.println();
		switch(params.getWide()) {
		case standard:
			print(config, standardFields);
			break;
		case wide:
			print(config, wideFields);
			break;
		case ultra:
			print(config, ultraFields);
		}
	}
	
	private void print(Config config, ConfigType[] configTypes) {
		for(ConfigType configType : configTypes) {
			System.out.println(configType.getClearName()+":");
			Field[] fields = Config.class.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(APIManagerConfigAnnotation.class)) {
					APIManagerConfigAnnotation annotation = field.getAnnotation(APIManagerConfigAnnotation.class);
					if(annotation.configType()==configType) {
						System.out.printf("%s %s: %s\n", annotation.name() , dots.substring(annotation.name().length()), getFieldValue(field.getName(), config));
					}
				}
			}
			System.out.println();
		}
	}
	
	private String getFieldValue(String fieldName, Config config) {
		try {
			PropertyDescriptor pd = new PropertyDescriptor(fieldName, config.getClass());
			Method getter = pd.getReadMethod();
			Object value = getter.invoke(config);
			return (value==null) ? "N/A" : value.toString();
		} catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				LOG.error(e.getMessage(), e);
			}
			return "Err";
		}
	}
}
