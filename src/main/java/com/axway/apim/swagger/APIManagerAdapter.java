package com.axway.apim.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.actions.RecreateToUpdateAPI;
import com.axway.apim.actions.UpdateExistingAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.api.IAPIDefinition;

/**
 * @author cwiechmann
 * This class is taking the API-Contract + API-Definition and imports 
 * it into the API. 
 * 
 * Internal note - to be removed, adjusted:
 * What we have to keep in mind:
 * - Import an API-Definition
 *   - the "Importer" should not take into consideration, if an API exists. Just imports it
 * - At certain place, the program must compare an existing API:
 *   - Identified with URI-Path + Version + (perhaps something else))
 *   - Maybe using Comparable Interface for that
 * - perhaps it makes sense to have an APIImportDefinition + ExistingAPIDefinition
 *   - Existing API-Definition is created based on the importAPI (looking up the API-Manager REST-API)
 *   - now two entities sharing the same interface (API-Definition, incl. the Contract) can be compared
 *   - or the existing API-Definition is just null
 * 
 * - both, can be handed over to the API-Manager-ImportHandler
 *   - it can compare ImportAPI vs. ExistingAPI
 *   - if "Existing == null" 
 *     - just import the new API
 *     
 * 
 * - Also must be possible:
 *   - Delete API
 *     - again ImportAPI is provided and existing must be found!
 *     - API-Contract status the API to be deleted, which will be synchronized with API-Manager
 *   - Deprecate API
 *     - API-Contract status the API as Deprecated, which will be synchronized with API-Manager
 *   - Handling Breaking vs. Non-Breaking-Changes
 *     - it is Internally defined what is breaking 
 *       - is Desired state vs. Actual state is breaking
 *         - which will be case quite often on the first development stage (having many deployment cycles)
 *         --> If the actual state is published: API-Developer must force the update
 *             and the version number must be different --> Otherwise error!
 *             The enforcement will be done based on the configured CI/CD Job not in the contract 
 *             to avoid forced updates by mistake on the Production environment
 *       - If Non-Breaking
 *         - the program will just update the existing API
 *         - BUT: If the desired API only contains changes, that can be applied to the existing API
 *                (like the description today, later Tags, Custom-Props)
 *                we update the existing API
 *                Or if just the state changes from Unpublished, to Published, etc. 
 *         - incl. Deprecation and removal of the existing API (should is be configurable?)
 *           --> How should this be configured (not in the contract)
 *          
 *   
 *   - The overall purpose (just came in my mind):
 *   The Swagger-Tool must basically synchronize the desired state (as provided with Contract + Swagger) 
 *   with the Actual State (as actually represented by the API-Manager for that API)
 */
public class APIManagerAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	private boolean enforceBreakingChange = false;
	
	public APIManagerAdapter() {
		super();
		this.enforceBreakingChange = CommandParameters.getInstance().isEnforceBreakingChange();
	}

	public void applyChanges(APIChangeState changeState) {
		// No existing API found (means: No match for APIPath), creating a complete new
		if(!changeState.getActualAPI().isValid()) {
			// --> CreateNewAPI
			LOG.info("No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState);
		// We do have a breaking change!
		} else {
			LOG.info("Going to update existing API: " + changeState.getActualAPI().getApiName() +" (Version: "+ changeState.getActualAPI().getApiVersion() + ")");
			if(!changeState.hasAnyChanges()) {
				LOG.warn("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				throw new RuntimeException();
			}			
			if (changeState.isBreaking()) {
				LOG.info("Recognized the following breaking changes: " + changeState.getBreakingChanges() + 
						" plus Non-Breaking: " + changeState.getNonBreakingChanges());
				if(changeState.getActualAPI().getStatus().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
					LOG.error("Applying ALL changes on existing UNPUBLISHED API.");
					UpdateExistingAPI updateAPI = new UpdateExistingAPI();
					updateAPI.execute(changeState);
					return;
				} else {
					if(enforceBreakingChange) {
						if(changeState.isUpdateExistingAPI()) {
							LOG.info("Updating existing API with breaking changes: " + changeState.getBreakingChanges() + 
									" plus Non-Breaking: " + changeState.getNonBreakingChanges());
						} else {
							LOG.info("Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
									+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for PUBLISHED API. Recreating it!");
							RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
							recreate.execute(changeState);
						}
					} else {
						LOG.error("A breaking change can't be applied without enforcing it!");
						return;
					}
				}
			// A NON-Breaking change
			} else if(!changeState.isBreaking()) {
				if(changeState.isUpdateExistingAPI()) {
					// Contains only changes, that can be applied to the existing API (even depends on the status)
					LOG.info("Updating existing API with Non-Breaking changes: " + changeState.getNonBreakingChanges());
				} else {
					// We have changes requiring a new API to be imported
					LOG.info("Create and Update API, delete existing");
				}
			}
		}
	}
}