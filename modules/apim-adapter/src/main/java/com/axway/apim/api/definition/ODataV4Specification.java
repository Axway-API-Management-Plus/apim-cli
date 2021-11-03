package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ODataV4Specification extends ODataSpecification {
	
	public ODataV4Specification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.ODATA_V4;
	}
	
	@Override
	public boolean configure() throws AppException {
		String specStart = new String(this.apiSpecificationContent, 0, 500).toLowerCase();
		if(specStart.contains("edmx") && specStart.contains("4.0")) {
			throw new AppException("Detected OData V4 specification, which is not yet supported by the APIM-CLI.\n"
					+ "                    Please upvote the following issue:\n"
					+ "                    https://github.com/Axway-API-Management-Plus/apim-cli/issues/234\n"
					+ "                    if you have a need for OData V4 support", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
		}
		return false;
	}

}
