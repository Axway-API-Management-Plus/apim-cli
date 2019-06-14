package com.axway.apim.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.App;

public class APIPropertiesExport {
	
	private static Logger LOG = LoggerFactory.getLogger(App.class);
	
	Properties properties = new Properties();
	
	String propertyComment;
	
	private static APIPropertiesExport instance = null;

	private APIPropertiesExport() {
		super();
	}
	
	public static APIPropertiesExport getInstance() {
		if(instance==null) {
			instance = new APIPropertiesExport();
		}
		return instance;
	}
	
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void setPropertyComment(String propertyComment) {
		this.propertyComment = propertyComment;
	}
	
	public void store() {
		if(properties.isEmpty()) return;
		String exportFile = CommandParameters.getInstance().getDetailsExportFile();
		if(exportFile==null) return;
		File file = new File(exportFile);
		try {
			if(!file.isAbsolute()) {
				String configFile = CommandParameters.getInstance().getValue("contract");
				String baseDir = new File(configFile).getCanonicalFile().getParent();
				file = new File(baseDir + File.separator + exportFile);
			}
			properties.store(new FileOutputStream(file), this.propertyComment);
			LOG.info("Created API-Properties file: '"+file+"'");
		} catch (Exception e) {
			LOG.error("Cant create API-Properties file based on filename: '"+exportFile+"'", e);
		}
	}
}
