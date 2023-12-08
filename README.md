# Axway API-Management CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Latest Release](https://img.shields.io/github/v/release/Axway-API-Management-Plus/apim-cli)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Axway-API-Management-Plus_apim-cli)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Axway-API-Management-Plus_apim-cli)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Axway-API-Management-Plus_apim-cli&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli)


![downloads](https://img.shields.io/github/downloads/Axway-API-Management-Plus/apim-cli/total)


This CLI allows you to control the Axway API management solution without access through the Web UI. You can call the CLI manually or integrate it into a CI/CD pipeline. The CLI is based on [Swagger-Promote](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote), which has been refactored to support more use-cases than only API-Import and -Export.

## Everything as code
The basic idea for the so-called "Everything as code" approach is that the desired state of something (API, Application, etc.) is declared in a configuration file which stored in your version management system. A tool, like this CLI, is then replicating that declared state to become the actual state in the target environment. This is used for instance to manage complex Cloud-Infrastructure called Infrastructure as code. You may read more here: https://hackernoon.com/everything-as-code-explained-0ibg32a3  
  
This CLI is following the same approach for your APIs, Applications, etc. Everything is declared as code in a config file and stored in your version management system. The CLI transfers this desired state manually or automatically via a pipeline into the API management platform to become the actual state.  
Additionally the CLI supports basic administrative commands for instance to list and filter entities and perform simple tasks. More will be added very soon.    

Watch this video (28 min): https://youtu.be/2i8i1zMAMps to get an overview + demo.

With that, an API developer or the operations team is just providing the desired state configuration of the API, Application. When checked in, the [CI/CD-Pipeline](https://github.com/Axway-API-Management-Plus/apim-cli/wiki/7.-Pipeline-integration) picks it up and replicates it into the API Manager. 

![API Manager CLI overview](https://github.com/Axway-API-Management-Plus/apim-cli/blob/develop/misc/images/apim-cli-overview.png )

## Supported commands

The CLI is flexible and is extended with new functions via modules. These can be accessed and discovered via the CLI on the basis of groups and their commands.  
To get an overview about the groups just call `apim`:
```
Available commands and options:
apim app             Manage your applications 
apim settings        Manage your API-Manager Config/Remote-Hosts & Alerts
apim org             Manage your organizations
apim api             Manage your APIs 
apim user            Manage your users 
```
To get for instance a `wide` list of APIs on the stage: `prod` execute the following command:
```
apim api get -s prod -wide
```
```
....
+--------------------------------------+-----------------+----------------+---------+---------------------+-------------+-------------+----------+----------------------+
| API-Id                               | Path            | Name           | Version | V-Host              | State       | Security    | Policies | Organization         |
+--------------------------------------+-----------------+----------------+---------+---------------------+-------------+-------------+----------+----------------------+
| 518b15c9-350c-47d8-9ad6-16ce02ef9dfe | /vhost-test-950 | VHost Test 950 |   1.0.0 | api123.customer.com | unpublished | passThrough | None     | API Development 5538 |
| 39b7b2aa-7df8-44e0-b399-4e9d59dbad6d | /vhost-test-411 | VHost Test 411 |   1.0.0 | api123.customer.com |   published | passThrough | None     | API Development 5916 |
+--------------------------------------+-----------------+----------------+---------+---------------------+-------------+-------------+----------+----------------------+
......
```

To get a list of commands for each group call for instance `apim app`

The CLI is flexible and already provides a lot of commands & utility functions. For an up-to-date list of the supported groups and commands, see the [documentation](https://github.com/Axway-API-Management-Plus/apim-cli/wiki#supported-commands).

## Quality assurance process
By using this CLI to control your Axway API management infrastructure it becomes a key component of your CI/CD process. Product quality is therefore very important so that you can be confident this CLI is doing what it's supposed to do.  
To achieve this quality bar, the tools was developed from the beginning in a way that it can be tested fully automatically.  
In addition to a number of executed unit-tests, sophisticated integration tests are performed. These integration tests are executed against different API-Management versions and consists of a number of different scenarios. Each scenrioa contains various __Desired__ states, which are transferred into __Actual__ state by the CLI and finally checked if the Actual State in the API-Manager is as expected. This is performed for APIs and applications.   

The automated End-2-End test suite contains of __116__ different scenarios, which includes more than __284__ executions of CLI (Import & Export) following each by a validation step. The test suite is executed at Travis CI for the following versions and you may check yourself what is done by clicking on the badge icon:  

| Version      | Branch                 | Status | Comment                                                            | 
|:-------------|:-----------------------| :---:  |:-------------------------------------------------------------------|
| 7.7-20230130 | develop                |![Build Status](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg)| Requires version >=1.14.3                                          |   
| 7.7-20230830 | test-with-7.7-20230830 |![Build Status](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg)| Requires version >=1.14.2                                          |   
| 7.7-20230530 | test-with-7.7-20230530 |![Build Status](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg)| Requires version >=1.14.0                                          |   
| 7.7-20230228 | test-with-7.7-20230228 |![Build Status](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg)| Requires version >=1.13.4                                          |
| 7.7-20221130 | test-with-7.7-20221130 |[![APIM CLI Integration test](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg)](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml)| Requires version >=1.13.2, Multi-Org supported from version 1.13.3 |
| 7.7-20220830 | test-with-7.7-20220830 |[![APIM CLI Integration test](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml/badge.svg?branch=test-with-7.7-20220830)](https://github.com/Axway-API-Management-Plus/apim-cli/actions/workflows/integration-test.yml)| Requires version >=1.13.0, Multi-Org is not yet supported          |
| 7.7-20220530 | test-with-7.7-20220530 | [![Build Status](https://img.shields.io/travis/Axway-API-Management-Plus/apim-cli/test-with-7.7-20211130)](https://app.travis-ci.com/github/Axway-API-Management-Plus/apim-cli/branches)| Requires version >=1.12.0, Multi-Org is not yet supported          |
| 7.7-20220228 | test-with-7.7-20220228 | [![Build Status](https://img.shields.io/travis/Axway-API-Management-Plus/apim-cli/test-with-7.7-20220228)](https://app.travis-ci.com/github/Axway-API-Management-Plus/apim-cli/branches)| Requires version >=1.10.1, Multi-Org is not yet supported          |
| 7.7-20211130 | test-with-7.7-20211130 | [![Build Status](https://img.shields.io/travis/Axway-API-Management-Plus/apim-cli/test-with-7.7-20211130)](https://app.travis-ci.com/github/Axway-API-Management-Plus/apim-cli/branches)| Requires version >=1.3.11, Multi-Org is not yet supported          |


At least version 7.7-20211130 is required.  

## Get started

To get started, you have several options to [download the CLI](https://github.com/Axway-API-Management-Plus/apim-cli/wiki/1.-How-to-get-started) and then just run the CLI as shown in the example below

```
scripts\apim api import -c samples/basic/minimal-config-api-definition.json -s api-env
```
This command is reading the API-Management platform configuration details from the [environment file](https://github.com/Axway-API-Management-Plus/apim-cli/wiki/8.2.-Environment-property-files): `env.api-env.properties` and replicates the given desired API state in the configuration file: `minimal-config-api-definition.json` into the API-Management platform. 

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
 
