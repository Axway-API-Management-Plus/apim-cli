package com.axway.apim.api.model;

import java.util.List;

public class CustomProperty {
	
	private String label;
	
	private String type;
	
	private Boolean disabled;
	
	private Boolean required;
	
	private String help;
	
	private List<Option> options;
	
	private String defaultValue;
	
	private String regex;
	
	private Integer minValue;
	
	private Integer maxValue;
	
	private Integer decimalPlaces;
	
	private String error;
	
	private String custom;
	
	private CustomPropertyPermission permissions;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}
	
	public CustomPropertyPermission getPermissions() {
		return permissions;
	}

	public void setPermissions(CustomPropertyPermission permissions) {
		this.permissions = permissions;
	}

	public static class CustomPropertyPermission {
		private Permissions admin;
		private Permissions oadmin;
		private Permissions user;
		public Permissions getAdmin() {
			return admin;
		}
		public void setAdmin(Permissions admin) {
			this.admin = admin;
		}
		public Permissions getOadmin() {
			return oadmin;
		}
		public void setOadmin(Permissions oadmin) {
			this.oadmin = oadmin;
		}
		public Permissions getUser() {
			return user;
		}
		public void setUser(Permissions user) {
			this.user = user;
		}
	}
	public static class Permissions {
		private String read;
		private String write;
		
		public String getRead() {
			return read;
		}
		public void setRead(String read) {
			this.read = read;
		}
		public String getWrite() {
			return write;
		}
		public void setWrite(String write) {
			this.write = write;
		}
	}
	
	public static class Option {
		String value;
		String label;
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
	}
}
