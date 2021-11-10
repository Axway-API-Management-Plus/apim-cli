package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ODataV3Specification extends ODataSpecification {
	
	public ODataV3Specification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.ODATA_V4;
	}
	
	@Override
	public boolean configure() throws AppException {
		String specStart = new String(this.apiSpecificationContent, 0, 500).toLowerCase();
		if(specStart.contains("edmx") && specStart.contains("3.0")) {
			throw new AppException("Detected OData V3 specification, which is not yet supported by the APIM-CLI.\n"
					+ "                                 | If you have a need for OData V3 support please upvote the following issue:\n"
					+ "                                 | https://github.com/Axway-API-Management-Plus/apim-cli/issues/235", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
		}
		return false;
	}

}
