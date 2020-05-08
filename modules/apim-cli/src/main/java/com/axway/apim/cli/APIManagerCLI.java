package com.axway.apim.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.axway.apim.APIImportApp;
import com.axway.apim.lib.APIMCLIServiceProvider;

/**
 * This class implements a pluggable CLI interface that allows to dynamically add new 
 * CLI services. A CLI-Service is for instance the management of Client-Apps or the KPS. Such module needs to implement 
 * the APIMCLIServiceProvider interface and make the implementation available through the ServiceLoader approach 
 * (META-INF/services/ com.axway.apim.lib.APIMCLIServiceProvider). 
 * With that in place, the module is detected automatically and becomes part of the CLI.
 */
public class APIManagerCLI {
	
	private static String APIM_CLI_CDM = "apim";
	
	/**
	 * A Service-Implementation belongs a certain group (like api or app). This map, groups services against their groups.
	 */
	HashMap<String, List<APIMCLIServiceProvider>> servicesMappedByGroup = new HashMap<String, List<APIMCLIServiceProvider>>();
	
	/**
	 * Is set by parseArguments when the first parameter is set. For instance: apim api - With that the selected service group is api and will 
	 * contain all CLI services related to API handling. 
	 */
	List<APIMCLIServiceProvider> selectedServiceGroup = null;
	
	/**
	 * When in addition to the service group id, also a valid method is passed, this is the final service implementation to call. 
	 */
	APIMCLIServiceProvider selectedService = null;
	
	/**
	 * The parsed method as part of a service.
	 */
	String selectedMethod = null;

	public APIManagerCLI(String[] args) {
		super();
		ServiceLoader<APIMCLIServiceProvider> loader = ServiceLoader
			      .load(APIMCLIServiceProvider.class);
		Iterator<APIMCLIServiceProvider> it = loader.iterator();
		while(it.hasNext()) {
			APIMCLIServiceProvider cliService = it.next();
			List<APIMCLIServiceProvider> providerList = servicesMappedByGroup.get(cliService.getGroupId());
			if(providerList==null) {
				providerList = new ArrayList<APIMCLIServiceProvider>();
				providerList.add(cliService);
				servicesMappedByGroup.put(cliService.getGroupId(), providerList);
			} else {
				providerList.add(cliService);
			}
		}
		parseArguments(args);
	}


	public static void main(String[] args) {
		APIManagerCLI cli = new APIManagerCLI(args);
		cli.run(args);
	}
	
	private void parseArguments(String[] args) {
		if(args==null || args.length<1) return;
		for(int i=0; i<args.length; i++) {
			// Skip the first argument when it's choco
			if(args[i]==null || args[i].equals("choco")) continue;
			if(servicesMappedByGroup.get(args[i])!=null) {
				this.selectedServiceGroup = servicesMappedByGroup.get(args[i]);
				if(args.length <= i+1) break;
				String method = args[i+1];
				for(APIMCLIServiceProvider service : selectedServiceGroup) {
					if(method.equals(service.getMethod())) {
						this.selectedService = service;
						this.selectedMethod = method;
					}
				}
				break;
			}
		}
	}
	
	void printUsage() {
		System.out.println("The Axway API-Management CLI supports the following commands.");
		System.out.println("To get more information for each command, please run for instance: 'apim api'");
		System.out.println("");
		System.out.println("Available commands and options: ");
		Iterator<String> it = servicesMappedByGroup.keySet().iterator();
		if(this.selectedServiceGroup==null) {
			while(it.hasNext()) {
				String key = it.next();
				// We just take the first registered service for a group to retrieve the group description
				System.out.println(APIM_CLI_CDM + " " + key + " - " + servicesMappedByGroup.get(key).get(0).getGroupDescription());
			}
		} else {
			for(APIMCLIServiceProvider service : this.selectedServiceGroup) {
				System.out.println(APIM_CLI_CDM + " " + service.getGroupId()  + " " + service.getMethod() + " - " + service.getDescription());
			}
		}
	}
	
	void run(String[] args) {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("API-Manager CLI: "+APIImportApp.class.getPackage().getImplementationVersion());
		System.out.println("                                                                        ");
		System.out.println("To report issues or get help, please visit: ");
		System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote");
		System.out.println("------------------------------------------------------------------------");
		if(this.selectedMethod==null) {
			this.printUsage();
		} else {
			System.out.println("Running module: " + this.selectedService.getName() + " "+this.selectedService.getVersion());
			System.out.println("------------------------------------------------------------------------");
			this.selectedService.execute(args);
		}
	}

}
