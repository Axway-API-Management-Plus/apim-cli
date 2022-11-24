package com.axway.apim.api.apiSpecification;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ODataV3Specification extends ODataSpecification {


	@Override
	public void updateBasePath(String basePath, String host) {

	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.ODATA_V4;
	}
	
	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		String specStart = new String(apiSpecificationContent, 0, 500).toLowerCase();
		if(specStart.contains("edmx") && specStart.contains("3.0")) {
			throw new AppException("Detected OData V3 specification, which is not yet supported by the APIM-CLI.\n"
					+ "                                 | If you have a need for OData V3 support please upvote the following issue:\n"
					+ "                                 | https://github.com/Axway-API-Management-Plus/apim-cli/issues/235", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
		}
		return false;
	}

}
