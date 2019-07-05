package com.axway.apim.lib;

import java.io.BufferedReader;
import java.io.FileReader;

public class Utils {
	public static String getAPIDefinitionUriFromFile(String pathToAPIDefinition) throws AppException {
		String uriToAPIDefinition = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToAPIDefinition));
			uriToAPIDefinition = br.readLine();
			return uriToAPIDefinition;
		} catch (Exception e) {
			throw new AppException("Can't load file:" + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				br.close();
			} catch (Exception ignore) {}
		}
	}
}
