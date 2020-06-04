# Axway API-Management CLI

This CLI tool allows you to control the Axway API management solution without access through the Web UI. You can call the CLI manually or integrate it into a CI/CD pipeline. The CLI is based on [Swagger-Promote](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote), which has been refactored to support more use-cases than only APIs.

## Everything as code
The basic idea for the so-called "... as code" approach is that the desired state of something is declared in a configuration file which stored in your version management system. A tool is replicating that declared state to become the actual state. This is used for instance to manage a complex Cloud-Infrastructure called Infrastructure as code. You may read more here: https://hackernoon.com/everything-as-code-explained-0ibg32a3  
  
This CLI is following the same approach for your APIs, Applications, etc. Everything is declared as code in a configuration file and stored in your version management system. The CLI transfers this desired state manually or automatically via a pipeline into the API management platform to become the actual state. 

Watch this video (28 min): https://youtu.be/2i8i1zMAMps to get an overview + demo.

With that, an API developer is just providing the desired state configuration of the API, Application. When checked in, the [CI/CD-Pipeline](https://github.com/Axway-API-Management-Plus/apim-cli/wiki/9.-Jenkins-Integration-with-GitHub-&-Bitbucket) picks it up and replicates it into the API Manager. 

![API Manager Swagger-Promote overview](https://github.com/Axway-API-Management-Plus/apim-cli/blob/develop/misc/images/apimanager-swagger-promote-overview.png )

The following commands are supported

## APIs 
| Command       | Comment | 
| :---        | :---  |
|**import**|Replicates an API into the API-Manager |  
|**export**|Exports one or more APIs from a running API-Manager |  

## Applications
| Command       | Comment | 
| :---        | :---  |
|**import**|Replicates an application into the API-Manager |  
|**export**|Exports one or more applications from a running API-Manager | 

## Quality assurance process
By using Swagger Promote to control your Axway API management infrastructure it becomes a key component of your CI/CD process. Product quality is therefore very important so that you can be confident that Swagger Promote is doing what it's supposed to do.  
To achieve this quality bar, Swagger-Promote was developed from the beginning in a way that it can be tested fully automatically. The test process consists of different scenarios, which contain various __API-Desired__ states, which are transferred into __Actual__ state by Swagger-Promote and finally checked if the Actual State in the API-Manager is correct.  

With the most recent released version 1.6.5 the automated End-2-End test suite contains of __104__ different scenarios, which includes more than __250__ executions of Swagger-Promote (Import & Export) following each by a validation step. The test suite is executed at Travis CI for the following versions:  

| Version       | Branch               | Status | Comment | 
| :---          | :---                 | :---:  | :--- |
| 7.6.2 SP2     | test-with-7.6.2-SP2  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.6.2-SP2)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.6.2 SP3     | test-with-7.6.2-SP3  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.6.2-SP3)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.6.2 SP4     | develop  | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=develop)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.7           | test-with-7.7        | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.7)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.7 SP1       | test-with-7.7-SP1    | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.7-SP1)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.7 SP2       | test-with-7.7-SP2    | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.7-SP2)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.7 20200130  | test-with-7.7-20200130    | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.7-20200130)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)||
| 7.7 20200331  | test-with-7.7-20200331    | [![Build Status](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote.svg?branch=test-with-7.7-20200331)](https://travis-ci.org/Axway-API-Management-Plus/apimanager-swagger-promote/branches)|Also the March-Release works, but some of the Quota-Tests are a flaky.|

Also version 7.5.3 is supported, but not fully automated tested.  


## Changelog
See [change log](CHANGELOG.md)

## Contributing

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.  
Also please read this page on [how to contribute](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki/7.1-Contribute-to-this-project) to this project.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"


## License
[Apache License 2.0](/LICENSE)
