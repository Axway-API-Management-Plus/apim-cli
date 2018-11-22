# Swagger based promotion for Axway API-Manager V7

This project provides you with a small Java-Program that simplifies your CI/CD pipeline with the Axway API-Manager Version 7.x. 

The program works based on a Swagger-Definition + an API-Contract and replicates this into the API-Manager. Consider the Swagger-Definition + API-Contract as the "__desired__" state and API-Manager has the "__actual__" state. This program will compare both, the desired with the actual state, and performs all neccassary actions to bring the API-Manager into the desired state.

With that, API-Developer is just providing the Swagger-File (e.g. Code-Generated or using a Swagger-Editor) and the API-Contract. When checked in, the CI/CD-Pipelines picks it up and replicates it into the API-Manager. 
This includes Zero-Downtime-Upgrade of existing applications, which might have been subscribed to an API.

![API-Manager Swagger-Promote overview]( https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/blob/master/src/lib/images/apimanager-swagger-promote-overview.png )

The ultimate goal is to fully control all properties of an API in API-Manager based on the Swagger- + API-Contract-File.
Today the following is already supported:
- State-Handling (Unpblished, Published, Dreprecated & Deleted)
- API-Summary
- API-Image 
- API-Version
- API-Path
- Inbound-Security settings (Default for all methods)
  - incl. all custom settings (e.g. API-Key settings, etc.)
- Outbound-Custom-Policies
  - Routing, Request, Response, FaultHandler
- Outbound Authentication
- CORS-Setup
- V-Host support
- Tags

The following is on the roadmap:
- API-description
- API-Method description
- Custom-Properties
- Method-Level settings such as security, Custom-Policies, etc.

Build and tested with API-Manager 7.6.2 SP1 at Travis CI: [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=master)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote)

## Changelog
- 0.0.1 - 19.11.2018
  - Initial version


## Limitations/Caveats
- see the not completed features

## Contributing

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"


## License
[Apache License 2.0](/LICENSE)
