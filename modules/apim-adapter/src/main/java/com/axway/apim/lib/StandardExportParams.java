package com.axway.apim.lib;

import java.nio.file.Paths;

public class StandardExportParams extends CoreParameters {
	
	public enum Wide {
		standard, 
		wide, 
		ultra
	}
	
	public enum OutputFormat {
		console, 
		json, 
		csv,
		yaml,
		dat;
		
		public static OutputFormat getFormat(String name) {
			if(name == null) {
				return console;
			} else {
				valueOf(name);
			}
			return valueOf(name);
		}
	}
	
	private Wide wide;
	
	private Boolean deleteTarget;
	
	private String target;
	
	private OutputFormat outputFormat;
	
	public static synchronized StandardExportParams getInstance() {
		return (StandardExportParams)CoreParameters.getInstance();
	}
	
	public Wide getWide() {
		if(wide==null) return Wide.standard;
		return wide;
	}

	public void setWide(Wide wide) {
		this.wide = wide;
	}

	public OutputFormat getOutputFormat() {
		if(outputFormat==null) return OutputFormat.console;
		return outputFormat;
	}

	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public Boolean isDeleteTarget() {
		if(deleteTarget==null) return false;
		return deleteTarget;
	}

	public void setDeleteTarget(Boolean deleteTarget) {
		if(deleteTarget==null) return;
		this.deleteTarget = deleteTarget;
	}

	public String getTarget() {
		if(this.target==null) return Paths.get("").toAbsolutePath().toString();
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}