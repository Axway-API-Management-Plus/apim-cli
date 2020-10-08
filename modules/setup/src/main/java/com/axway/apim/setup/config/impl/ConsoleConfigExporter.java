package com.axway.apim.setup.config.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.APIManagerConfigAnnotation;
import com.axway.apim.lib.APIManagerConfigAnnotation.ConfigType;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.config.lib.ConfigExportParams;
import com.github.freva.asciitable.AsciiTable;

public class ConsoleConfigExporter extends ConfigResultHandler {
	
	APIManagerAdapter adapter;
	
	private String dots = ".....................................";
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;
	
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

	public ConsoleConfigExporter(ConfigExportParams params) {
		super(params);
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void export(APIManagerConfig config) throws AppException {
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
	
	private void print(APIManagerConfig config, ConfigType[] configTypes) {
		for(ConfigType configType : configTypes) {
			System.out.println(configType.getClearName()+":");
			Field[] fields = APIManagerConfig.class.getDeclaredFields();
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
	
	private String getFieldValue(String fieldName, APIManagerConfig config) {
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

	@Override
	public OrgFilter getFilter() throws AppException {
		return getBaseConfigFilterBuilder().build();
	}
}
