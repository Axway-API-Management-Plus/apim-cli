package com.axway.apim.metadata.export.formats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.state.IAPI;

public class CSVCustomPolicyDependencyReport extends AbstractReportFormat implements IReportFormat {
	
	private static Logger LOG = LoggerFactory.getLogger(CSVCustomPolicyDependencyReport.class);
	
	protected Map<String, List<IAPI>> usedRequestPolicies = new HashMap<String, List<IAPI>>();
	protected Map<String, List<IAPI>> usedRoutingPolicies = new HashMap<String, List<IAPI>>();
	protected Map<String, List<IAPI>> usedResponsePolicies = new HashMap<String, List<IAPI>>();
	protected Map<String, List<IAPI>> usedFaulthandlerPolicies = new HashMap<String, List<IAPI>>();
	
	public CSVCustomPolicyDependencyReport() {
		super();
	}

	@Override
	public void preProcessMetadata() throws AppException {
		// We need to know the API-Access for each Application
		initApplicationAPISubcription();
		LOG.info("Successfully loaded application subscriptions.");
		for(IAPI api : metaData.getAllAPIs()) {
			Iterator<OutboundProfile> it = api.getOutboundProfiles().values().iterator();
			while(it.hasNext()) {
				OutboundProfile profile = it.next();
				addAPIToPolicy(usedRequestPolicies, profile.getRequestPolicy(), api);
				addAPIToPolicy(usedRoutingPolicies, profile.getRoutePolicy(), api);
				addAPIToPolicy(usedResponsePolicies, profile.getResponsePolicy(), api);
				addAPIToPolicy(usedFaulthandlerPolicies, profile.getFaultHandlerPolicy(), api);
			}
		}

	}
	
	private void addAPIToPolicy(Map<String, List<IAPI>> usedPolicy, String policyName, IAPI api) {
		if(policyName==null || policyName.isEmpty()) return;
		if(usedPolicy.containsKey(policyName)) {
			List<IAPI> apis = usedPolicy.get(policyName);
			apis.add(api);
		} else {
			List<IAPI> apis = new ArrayList<IAPI>();
			apis.add(api);
			usedPolicy.put(policyName, apis);
		}
	}
	
	@Override
	public void exportMetadata() throws AppException {
		String filename = CommandParameters.getInstance().getValue("filename");
		Appendable appendable;
		CSVPrinter csvPrinter = null;
		try {
			File cvsFile = new File(filename);
			if(cvsFile.exists()) {
				LOG.info("Going to overwrite existing file: '"+cvsFile.getCanonicalPath()+"'");
			}
			appendable = new FileWriter(cvsFile);
			csvPrinter = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(
				"PolicyName", 
				"PolicyType", 
				"APIName",
				"APIStatus", 
				"APIVersion",
				"NoSubscribedApps", 
				"APIId"
			));
			writePolicyTypeToCSV(csvPrinter, usedRequestPolicies, "Request");
			writePolicyTypeToCSV(csvPrinter, usedRoutingPolicies, "Routing");
			writePolicyTypeToCSV(csvPrinter, usedResponsePolicies, "Response");
			writePolicyTypeToCSV(csvPrinter, usedFaulthandlerPolicies, "Faulthandler");
			LOG.info("Custom-Policy dependency information exported into file: '"+cvsFile.getCanonicalPath()+"'");
		} catch (IOException e) {
			throw new AppException("Cant open CSV-File for writing", ErrorCode.UNXPECTED_ERROR);
		} finally {
			if(csvPrinter!=null)
				try {
					csvPrinter.close(true);
				} catch (Exception ignore) {
					throw new AppException("Unable to close CSVWriter", ErrorCode.UNXPECTED_ERROR, ignore);
				}
		}
		
	}
	
	private void writePolicyTypeToCSV(CSVPrinter csvPrinter, Map<String, List<IAPI>> policies, String type) throws IOException, AppException {
		int i=0;
		Iterator<String> it = policies.keySet().iterator();
		while(it.hasNext()) {
			String policyName = it.next();
			List<IAPI> apis = policies.get(policyName);
			policyName = beautifyPolicyName(policyName);
			for(IAPI api : apis) {
				i++;
				int noOfApps = (api.getApplications()==null) ? 0 : api.getApplications().size();
				csvPrinter.printRecord(policyName, type, api.getName(), api.getState(), api.getVersion(), noOfApps, api.getId());
				if( i % 50 == 0 ){
					csvPrinter.flush();
				}
			}
		}
		csvPrinter.flush();
	}
}
