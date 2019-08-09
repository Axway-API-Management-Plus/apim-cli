# Swagger/WSDL based API promotion for Axway API-Manager V7

This project provides you with a tool that simplifies your DevOps experience with the Axway API-Manager Version 7.x. 

The program works based on the API-Definition (as Swagger or a WSDL (__Beta - Not for production__)) + an API-Configuration-File and replicates this "state" into the API-Manager. Consider the API-Definition + API-Config as the "__desired__" state and API-Manager has the "__actual__" state. This program will compare both, the desired with the actual state, and performs all necessary actions to bring the API-Manager API into the desired state.

Watch this video (28 min): https://youtu.be/2i8i1zMAMps to get an overview + demo.

With that, an API-Developer is just providing the Swagger-File or WSDL (e.g. Code-Generated or using a Swagger-Editor) and the API-Config. When checked in, the CI/CD-Pipelines picks it up and replicates it into the API-Manager. 
This includes __Zero-Downtime-Upgrade of existing applications__, which might have an active subscription to an API. Learn more in the [documentation](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki).

![API-Manager Swagger-Promote overview]( https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/blob/develop/misc/images/apimanager-swagger-promote-overview.png )

Today the following API-Properties are already supported and can be controlled externally:
- State-Handling (Unpublished, Published, Deprecated & Deleted)
- API-Summary, API-description
- API-Image 
- API-Version
- API-Path
- API-Inbound-Security settings 
  - incl. all custom settings (e.g. API-Key settings, etc.)
- Outbound-Custom-Policies
  - Routing, Request, Response, FaultHandler
- Outbound Authentication
- Backend Base-Path
- CORS-Setup
- V-Host
- Tags
- Custom-Properties
- Quota-Management (Application- & System-Default-Quota)  
  - also handling Application Quotas manually configured while updating an API
- Client-Organization handling  
- Query-String routing  

Improving the API-Development experience during the API-Design phase leveraging the Stoplight integration. [Learn more](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki/Stoplight-Integration).

Build and tested with API-Manager 7.6.2 SP2 at Travis CI:  
Develop: [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=develop)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote)
Master: [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=master)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote)  
End-2-End-Tests simulate real world use cases by calling and validating API-Manager promote __161__ times.  

## Install
- Download the latest [release](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/releases) or get it from  [![Maven Central](https://img.shields.io/maven-central/v/com.github.axway-api-management-plus.swagger-promote/axway-swagger-promote-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.axway-api-management-plus.swagger-promote%22%20AND%20a:%22axway-swagger-promote-core%22)  
- extract the tar.gz file  
- make sure you have JRE 8 installed and setup JAVA_HOME environment variable

## Usage
- run the script scripts/run-swagger-import.sh to see the basic usage and some samples
- more information can be found in the project [wiki](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki) 

## Changelog
- 1.0.0 - 12.12.2018
  - Initial version that supports all API-Properties besides method level settings
- 1.0.1 - 13.12.2018
  - Added support for API-Manager 7.5.3 plus minor fixes
- 1.1.0 - 18.12.2018
  - Added support for Quota-Management plus minor fixes
- 1.2.0 - 20.12.2018
  - Added support for API-Outbound AuthN and support to configure the API-Backend-Basepath
- 1.3.0 - 08.03.2019
  - Added support for Organization- & Application-Management
- 1.4.0 - 14.03.2019
  - Added support to refer a Swagger-File from a URL instead of the local File-System only
- 1.4.1 - 20.03.2019
  - Stabilized handling of Client-Orgs and Client-Apps (added support for modes: add|replace|ignore)
- 1.4.2 - 03.04.2019
  - Added support for WSDLs API-Definitions, API-Manager port now a parameter, validate Admin-Role user is used
- 1.5.0 - 09.04.2019
  - Added support for Org-Admins and option to externalize parameters into environment properties
- 1.5.1 - 30.04.2019
  - Validate the given API-Organization & Now existing application quotas are preserved
- 1.5.2 - 27.05.2019
  - Added compatibility for 7.6.2 SP3, App-Quota-Handling improved, Support for Query-String-Option
- 1.5.3 - 01.07.2019
  - Now available in Maven-Central, API-Metadata export module, Outbound SSL support
- 1.6.0 - 12.07.2019
  - Added support for method level overrides, Rollback feature


## Limitations/Caveats
- API-Method-Level description is not yet supported
- Deep merge of stage-config files isn't supported
- Support for WSDL is still Beta

## Contributing

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.  
Also please read this page on [how to contribute](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki/7.1-Contribute-to-this-project) to this project.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"


## License
[Apache License 2.0](/LICENSE)
