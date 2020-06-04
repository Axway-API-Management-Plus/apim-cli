# Axway API-Management CLI

This CLI tool allows you to control the Axway API management solution without access through the Web UI. You can call the CLI manually or integrate it into a CI/CD pipeline. The CLI is based on [Swagger-Promote](https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote), which has been refactored to support more use-cases than only APIs.

## Everything as code
The basic idea for the so-called "... as code" approach is that the desired state of your API, Application, etc. is stored as code in a configuration file in the version management system or your choice. The CLI transfers this desired state manually or automatically via a pipeline into the API management platform. 

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
