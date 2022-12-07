package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

public class ExportResult extends Result {
	private final List<String> exportedFiles = new ArrayList<>();

	public List<String> getExportedFiles() {
		return exportedFiles;
	}
	
	public void addExportedFile(String filename) {
		exportedFiles.add(filename);
	}
}
