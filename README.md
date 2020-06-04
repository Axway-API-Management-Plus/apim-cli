# Axway API-Management CLI

This CLI tool allows you to control the Axway API management solution without access via the Web UI. In principle, two methods are feasible:
## APIs as code
They define how their APIs are to be managed in API management and the CLI replicates this desired state manually controlled or via a pipeline into the platform.  
### The following entities can be managed as code:
- APIs incl. Application subscriptions, Quotas
- Applications incl. security credentials, subscription to APIs
## Queries and administrative tasks
You can make various requests, for example you can display APIs that have the status "Pending approval" and then approve them if appropriate. Or you can display which policies are currently available in the system and the APIs they use.
### Supported commands
> Part of the roadmap and will be added soon

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
