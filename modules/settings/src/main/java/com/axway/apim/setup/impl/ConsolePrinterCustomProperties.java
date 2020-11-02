package com.axway.apim.setup.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.errorHandling.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

public class ConsolePrinterCustomProperties {
	
	protected static Logger LOG = LoggerFactory.getLogger(ConsolePrinterCustomProperties.class);
	
	APIManagerAdapter adapter;
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	public ConsolePrinterCustomProperties() {
		try {
			adapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}

	public void export(Map<String, CustomProperty> customProperties, Type type) throws AppException {
		System.out.println();
		if(customProperties == null || customProperties.size()==0) {
			System.out.println("No custom properties configured for type: " + type.niceName);
			return;
		}
		System.out.println("Custom property for type: " + type.niceName);
		System.out.println(AsciiTable.getTable(borderStyle, getCustomPropertiesWithName(customProperties), Arrays.asList(
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getName()),
				new Column().header("Label").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getLabel()),
				new Column().header("Type").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getType()),
				new Column().header("Default-Value").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getDefaultValue()),
				new Column().header("Required").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getRequired().toString()),
				new Column().header("Options").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> getOptions(prop)), 
				new Column().header("RegEx").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getRegex())
				)));
	}
	
	private String getOptions(CustomPropertyWithName prop) {
		if(prop.getCustomProperty().getOptions() == null || prop.getCustomProperty().getOptions().size()==0) return "";
		return prop.getCustomProperty().getOptions().toString().replace("[", "").replace("]", "");
	}
	
	private List<CustomPropertyWithName> getCustomPropertiesWithName(Map<String, CustomProperty> customProperties) {
		List<CustomPropertyWithName> result = new ArrayList<CustomPropertyWithName>();
		Iterator<String> it = customProperties.keySet().iterator();
		while(it.hasNext()) {
			String customProperty = it.next();
			CustomProperty customPropertyConfig = customProperties.get(customProperty);
			CustomPropertyWithName propWithName = new CustomPropertyWithName(customPropertyConfig);
			propWithName.setName(customProperty);
			result.add(propWithName);
		}
		return result;
	}
	
	private class CustomPropertyWithName {
		private String name;
		
		private CustomProperty customProperty;

		public CustomPropertyWithName(CustomProperty customProperty) {
			super();
			this.customProperty = customProperty;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public CustomProperty getCustomProperty() {
			return customProperty;
		}
	}
}
