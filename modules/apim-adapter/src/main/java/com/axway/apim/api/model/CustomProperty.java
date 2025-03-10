package com.axway.apim.api.model;

import java.util.List;

public class CustomProperty {

	private String label;

	private String type;

	private boolean disabled;

	private boolean required;

	private String help;

	private List<Option> options;

	private String defaultValue;

	private String regex;

	private Integer minValue;

	private Integer maxValue;

	private Integer decimalPlaces;

	private String error;

	private Object custom;

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

	public boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean getRequired() {
		return required;
	}

	public void setRequired(boolean required) {
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

	public Object getCustom() {
		return custom;
	}

	public void setCustom(Object custom) {
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
		private boolean read;
		private boolean write;
		private boolean visible;
		public boolean getRead() {
			return read;
		}
		public void setRead(boolean read) {
			this.read = read;
		}
		public boolean getWrite() {
			return write;
		}
		public void setWrite(boolean write) {
			this.write = write;
		}
		public boolean getVisible() {
			return visible;
		}
		public void setVisible(boolean visible) {
			this.visible = visible;
		}
	}

	public static class Option {
		private String value;
		private String label;
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
		@Override
		public String toString() {
			return "[" + value + "]";
		}
	}
}
