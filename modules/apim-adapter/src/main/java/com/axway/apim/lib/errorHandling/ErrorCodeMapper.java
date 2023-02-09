package com.axway.apim.lib.errorHandling;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodeMapper {
	
	private final Map<ErrorCode, ErrorCode> myMap = new HashMap<>();
	
	
	public ErrorCodeMapper() {
		super();
	}

	public void setMapConfiguration(String mapConfiguration) {
		if(mapConfiguration==null) return;
		String[] codeMaps = mapConfiguration.split(",");
		Map<Integer, ErrorCode> allErrorCodes = new HashMap<>();
		if(codeMaps.length>0) {
			for(ErrorCode code : ErrorCode.values()) {
				allErrorCodes.put(code.getCode(), code);
			}
		}
		for(String map : codeMaps) {
			String[] config = map.split(":");
			int inputCode = Integer.parseInt(config[0].trim());
			int outputCode = Integer.parseInt(config[1].trim());
			myMap.put(allErrorCodes.get(inputCode), allErrorCodes.get(outputCode));
		}
	}

	public ErrorCode getMapedErrorCode(ErrorCode code) {
		return myMap.getOrDefault(code, code);
	}
}
