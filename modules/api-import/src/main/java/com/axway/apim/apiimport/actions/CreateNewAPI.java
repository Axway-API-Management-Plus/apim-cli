package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.state.APIChangeState;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.apiimport.rollback.RollbackAPIProxy;
import com.axway.apim.apiimport.rollback.RollbackBackendAPI;
import com.axway.apim.apiimport.rollback.RollbackHandler;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.errorHandling.AppException;

/**
 * This class is used by the {@link APIImportManager#applyChanges(APIChangeState)} to create a new API.
 * It's called, when an existing API can't be found.
 *
 * @author cwiechmann@axway.com
 */
public class CreateNewAPI {

	static Logger LOG = LoggerFactory.getLogger(CreateNewAPI.class);

	public void execute(APIChangeState changes, boolean reCreation) throws AppException {

		API createdAPI = null;
		
		APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
		//APIManagerQuotaAdapter quotaAdapter = APIManagerAdapter.getInstance().quotaAdapter;

		//Transaction context = Transaction.getInstance();
		RollbackHandler rollback = RollbackHandler.getInstance();

		// During Re-Creation we have to Re-Init the Application-State
		//if(reCreation) APIManagerAdapter.getInstance().setAllApps(null);

		// Force to initially update the API into the desired state!
		//List<String> changedProps = getAllProps(changes.getDesiredAPI());

		VHostManager vHostManager = new VHostManager();
		API createdBEAPI = apiAdapter.importBackendAPI(changes.getDesiredAPI());
		rollback.addRollbackAction(new RollbackBackendAPI(createdBEAPI));

		try {
			changes.getDesiredAPI().setApiId(createdBEAPI.getApiId());
			createdAPI = apiAdapter.createAPIProxy(changes.getDesiredAPI());
		} catch (Exception e) {
			throw e;
		}
		rollback.addRollbackAction(new RollbackAPIProxy(createdAPI)); // In any case, register the API just created for a potential rollback
		APIChangeState.copyRequiredPropertisFromCreatedAPI(changes.getDesiredAPI(), createdAPI);

		try {
			// ... here we basically need to add all props to initially bring the API in sync!
			// But without updating the Swagger, as we have just imported it!
			createdAPI = apiAdapter.updateAPIProxy(changes.getDesiredAPI());

			// If an image is included, update it
			if(changes.getDesiredAPI().getImage()!=null) {
				apiAdapter.updateAPIImage(changes.getDesiredAPI());
			}
			// This is special, as the status is not a normal property and requires some additional actions!
			APIStatusManager statusManager = new APIStatusManager();
			statusManager.update(changes.getDesiredAPI(), createdAPI);
			apiAdapter.updateRetirementDate(createdAPI);

			if(reCreation && changes.getActualAPI().getState().equals(API.STATE_PUBLISHED)) {
				// In case, the existing API is already in use (Published), we have to grant access to our new imported API
				apiAdapter.upgradeAccessToNewerAPI(changes.getDesiredAPI(), changes.getActualAPI());
			}

			// Is a Quota is defined we must manage it
			new APIQuotaManager(changes.getDesiredAPI(), createdAPI).execute();

			// Grant access to the API
			new ManageClientOrgs(changes.getDesiredAPI(), createdAPI).execute(reCreation);

			// Handle subscription to applications
			new ManageClientApps(changes.getDesiredAPI(), createdAPI, changes.getActualAPI()).execute(reCreation);

			// V-Host must be managed almost at the end, as the status must be set already to "published"
			vHostManager.handleVHost(changes.getDesiredAPI(), createdAPI);
		} catch (Exception e) {
			throw e;
		} finally {
			if(createdAPI==null) {
				LOG.warn("Cant create PropertiesExport as createdAPI is null");
			} else {
				APIPropertiesExport.getInstance().setProperty("feApiId", createdAPI.getId());
			}
		}
	}
}
