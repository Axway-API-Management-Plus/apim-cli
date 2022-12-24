package com.axway.apim.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class APIPropertiesExport {
	
	private static final Logger LOG = LoggerFactory.getLogger(APIPropertiesExport.class);
	
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

	public void store() {
		if(properties.isEmpty()) return;
		String exportFile = CoreParameters.getInstance().getDetailsExportFile();
		if(exportFile==null) return;
		File file = new File(exportFile);
		try {
			if(!file.isAbsolute()) {
				String configFile = StandardImportParams.getInstance().getConfig();
				String baseDir = new File(configFile).getCanonicalFile().getParent();
				file = new File(baseDir + File.separator + exportFile);
			}
			try(OutputStream outputStream = Files.newOutputStream(file.toPath())){
				properties.store(outputStream, this.propertyComment);
				LOG.info("Created API-Properties file: {}",  file);
			}
		} catch (Exception e) {
			LOG.error("Cant create API-Properties file based on filename: '"+exportFile+"'", e);
		}
	}
}
