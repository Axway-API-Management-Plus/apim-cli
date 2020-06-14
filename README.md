# Axway API-Management CLI

This CLI allows you to control the Axway API management solution without access through the Web UI. You can call the CLI manually or integrate it into a CI/CD pipeline. The CLI is based on [Swagger-Promote](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote), which has been refactored to support more use-cases than only API-Import and -Export.

## Everything as code
The basic idea for the so-called "Everything as code" approach is that the desired state of something (API, Application, etc.) is declared in a configuration file which stored in your version management system. A tool, like this CLI, is then replicating that declared state to become the actual state in the target environment. This is used for instance to manage complex Cloud-Infrastructure called Infrastructure as code. You may read more here: https://hackernoon.com/everything-as-code-explained-0ibg32a3  
  
This CLI is following the same approach for your APIs, Applications, etc. Everything is declared as code in a config file and stored in your version management system. The CLI transfers this desired state manually or automatically via a pipeline into the API management platform to become the actual state.  
Additionally the CLI supports basic administrative commands for instance to list and filter entities and perform simple tasks. More will be added very soon.    

Watch this video (28 min): https://youtu.be/2i8i1zMAMps to get an overview + demo.

With that, an API developer or the operations team is just providing the desired state configuration of the API, Application. When checked in, the [CI/CD-Pipeline](https://github.com/Axway-API-Management-Plus/apim-cli/wiki/9.-Jenkins-Integration-with-GitHub-&-Bitbucket) picks it up and replicates it into the API Manager. 

![API Manager Swagger-Promote overview](https://github.com/Axway-API-Management-Plus/apim-cli/blob/develop/misc/images/apimanager-swagger-promote-overview.png )

## Supported commands

The CLI is flexible and is extended with new functions via modules. These can be accessed and discovered via the CLI on the basis of groups and their commands.  
To get an overview about the groups just call `apim`:
```
Available commands and options:
apim app - Manage your applications
apim api - Manage your APIs
```
To get a list of commands for each group call for instance `apim app`:

For an up-to-date list of the supported groups and commands, see the [documentation](https://github.com/Axway-API-Management-Plus/apim-cli/wiki#supported-commands).

The CLI is flexible and more commands will be added. You can find a list of available commands in the .

## Quality assurance process
By using this CLI to control your Axway API management infrastructure it becomes a key component of your CI/CD process. Product quality is therefore very important so that you can be confident this CLI is doing what it's supposed to do.  
To achieve this quality bar, the tools was developed from the beginning in a way that it can be tested fully automatically.  
In addition to a number of executed unit-tests, sophisticated integration tests are performed. These integration tests are executed against different API-Management versions and consists of a number of different scenarios. Each scenrioa contains various __Desired__ states, which are transferred into __Actual__ state by the CLI and finally checked if the Actual State in the API-Manager is as expected. This is performed for APIs and applications.   

The automated End-2-End test suite contains of __113__ different scenarios, which includes more than __250__ executions of CLI (Import & Export) following each by a validation step. The test suite is executed at Travis CI for the following versions and you may check yourself what is done by clicking on the badge icon:  

| Version       | Branch               | Status | Comment | 
| :---          | :---                 | :---:  | :--- |
| 7.6.2 SP4     | develop  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apim-cli.svg?branch=develop)](https://travis-ci.org/Axway-API-Management-Plus/apim-cli/branches)||
| 7.7.0 SP2     | test-with-7.7-SP2  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apim-cli.svg?branch=test-with-7.7-SP2)](https://travis-ci.org/Axway-API-Management-Plus/apim-cli/branches)||
| 7.7-20200130    | test-with-7.7-20200130  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apim-cli.svg?branch=test-with-7.7-20200130)](https://travis-ci.org/Axway-API-Management-Plus/apim-cli/branches)||
| 7.7-20200331    | test-with-7.7-20200331  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apim-cli.svg?branch=test-with-7.7-20200331)](https://travis-ci.org/Axway-API-Management-Plus/apim-cli/branches)||

Version 7.5.3 is NOT supported.  

## Get started

To get started, you have several options to download the CLI and then just run the CLI as shown in the example below

```
scripts\apim api import -c samples/basic/minimal-config-api-definition.json -s api-env
```
This command is reading the API-Management platform configuration details from the environment file: `env.api-env.properties` and replicates the given desired API state in the configuration file: `minimal-config-api-definition.json` into the API-Management platform. 

Please see the [documentation](https://github.com/Axway-API-Management-Plus/apim-cli/wikis) for more information.  

## Changelog
See [change log](CHANGELOG.md)

## Contributing

![Contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen)  

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.  
Also please read this page on [how to contribute](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki/7.1-Contribute-to-this-project) to this project.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"


## License
[Apache License 2.0](/LICENSE)
