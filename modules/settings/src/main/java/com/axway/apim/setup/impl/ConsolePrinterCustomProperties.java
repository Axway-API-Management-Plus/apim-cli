package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConsolePrinterCustomProperties {


	APIManagerAdapter adapter;

	Character[] borderStyle = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS;

	private final List<CustomPropertyWithName> propertiesWithName;

	public ConsolePrinterCustomProperties() {
		try {
			adapter = APIManagerAdapter.getInstance();
			propertiesWithName = new ArrayList<>();
		} catch (AppException e) {
			throw new RuntimeException("Unable to get APIManagerAdapter", e);
		}
	}

	public void addProperties(Map<String, CustomProperty> customProperties, Type group) {
		if(customProperties == null || customProperties.isEmpty()) {
			Console.println("No custom properties configured for: " + group.niceName);
			return;
		}
		propertiesWithName.addAll(getCustomPropertiesWithName(customProperties, group));
	}

	public void printCustomProperties() {
		Console.println(AsciiTable.getTable(borderStyle, propertiesWithName, Arrays.asList(
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
		if(prop.getCustomProperty().getOptions() == null || prop.getCustomProperty().getOptions().isEmpty()) return "";
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

	private static class CustomPropertyWithName {
		private String name;

		private Type group;

		private final CustomProperty customProperty;

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
