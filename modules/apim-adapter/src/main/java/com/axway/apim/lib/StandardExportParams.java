package com.axway.apim.lib;

public class StandardExportParams extends CoreParameters {
	
	public static enum Wide {
		standard, 
		wide, 
		ultra
	}
	
	public static enum OutputFormat {
		console, 
		json, 
		csv;
		
		public static OutputFormat getFormat(String name) {
			if(name==null || valueOf(name)==null) return console;
			return valueOf(name);
		}
	}
	
	private Wide wide;
	
	private boolean deleteTarget;
	
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

	public boolean isDeleteTarget() {
		return deleteTarget;
	}

	public void setDeleteTarget(boolean deleteTarget) {
		this.deleteTarget = deleteTarget;
	}

	public String getTarget() {
		if(this.target==null) return ".";
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}