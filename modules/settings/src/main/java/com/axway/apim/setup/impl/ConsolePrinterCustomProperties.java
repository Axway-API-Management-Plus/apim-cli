package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.lib.errorHandling.AppException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConsolePrinterCustomProperties {
	
	protected static Logger LOG = LoggerFactory.getLogger(ConsolePrinterCustomProperties.class);
	
	APIManagerAdapter adapter;
	
	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;
	
	private List<CustomPropertyWithName> propertiesWithName;

	public ConsolePrinterCustomProperties() {
		try {
			adapter = APIManagerAdapter.getInstance();
			propertiesWithName = new ArrayList<>();
		} catch (AppException e) {
			LOG.error("Unable to get APIManagerAdapter", e);
			throw new RuntimeException(e);
		}
	}

	public void addProperties(Map<String, CustomProperty> customProperties, Type group) throws AppException {
		if(customProperties == null || customProperties.size()==0) {
			System.out.println("No custom properties configured for: " + group.niceName);
			return;
		}
		propertiesWithName.addAll(getCustomPropertiesWithName(customProperties, group));
	}
	
	public void printCustomProperties() {
		System.out.println(AsciiTable.getTable(borderStyle, propertiesWithName, Arrays.asList(
				new Column().header("Name").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(CustomPropertyWithName::getName),
				new Column().header("Group").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(CustomPropertyWithName::getGroup),
				new Column().header("Label").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getLabel()),
				new Column().header("Type").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getType()),
				new Column().header("Default-Value").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getDefaultValue()),
				new Column().header("Required").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getRequired().toString()),
				new Column().header("Options").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(this::getOptions),
				new Column().header("RegEx").headerAlign(HorizontalAlign.LEFT).dataAlign(HorizontalAlign.LEFT).with(prop -> prop.getCustomProperty().getRegex())
				)));		
	}
	
	private String getOptions(CustomPropertyWithName prop) {
		if(prop.getCustomProperty().getOptions() == null || prop.getCustomProperty().getOptions().size()==0) return "";
		return prop.getCustomProperty().getOptions().toString().replace("[", "").replace("]", "");
	}
	
	private List<CustomPropertyWithName> getCustomPropertiesWithName(Map<String, CustomProperty> customProperties, Type group) {
		List<CustomPropertyWithName> result = new ArrayList<>();
		for (String customProperty : customProperties.keySet()) {
			CustomProperty customPropertyConfig = customProperties.get(customProperty);
			CustomPropertyWithName propWithName = new CustomPropertyWithName(customPropertyConfig);
			propWithName.setName(customProperty);
			propWithName.setGroup(group);
			result.add(propWithName);
		}
		return result;
	}
	
	private class CustomPropertyWithName {
		private String name;
		
		private Type group;
		
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

		public String getGroup() {
			return group.niceName;
		}

		public void setGroup(Type group) {
			this.group = group;
		}
	}
}
