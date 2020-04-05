package com.axway.apim.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ErrorCodeMapper {
	
	private Map<ErrorCode, ErrorCode> myMap = new HashMap<ErrorCode, ErrorCode>();
	
	
	public ErrorCodeMapper() {
		super();
	}

	public ErrorCodeMapper(String mapConfiguration) {
		super();
		this.setMapConfiguration(mapConfiguration);
	}

	public void setMapConfiguration(String mapConfiguration) {
		if(mapConfiguration==null) return;
		String[] codeMaps = mapConfiguration.split(",");
		Map<Integer, ErrorCode> allErrorCodes = new HashMap<Integer, ErrorCode>();
		if(codeMaps.length>0) {
			for(ErrorCode code : Arrays.asList(ErrorCode.values())) {
				allErrorCodes.put(code.getCode(), code);
			}
		}
		for(String map : codeMaps) {
			String[] config = map.split(":");
			Integer inputCode = new Integer(config[0].trim());
			Integer outputCode = new Integer(config[1].trim());
			myMap.put(allErrorCodes.get(inputCode), allErrorCodes.get(outputCode));
		}
	}

	public ErrorCode getMapedErrorCode(ErrorCode code) {
		return (!myMap.containsKey(code)) ? code : myMap.get(code);
	}
}
