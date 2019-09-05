## API-Manager Swagger-Promote examples

This directory contains a number of examples, which can be executed out of the box and configuration fragments can taken 
over into your API-Configuration.  

All examples will be grouped into different sections to make it easier to find them.  

### Basic Use-cases
Creates the most simple API, without any special settings:  
`scripts\api-import.bat -a ../petstore.json -c samples/basic/minimal-config.json -h api-env -u apiadmin -p changeme`

Instead of providing the API externally, it's referenced as part of the API-Config:  
`scripts\api-import.bat -c samples/basic/minimal-config-api-definition.json -h api-env -u apiadmin -p changeme`

Creates a SOAP-Service API:    
`scripts\api-import.bat -a "http://www.dneonline.com/calculator.asmx?wsdl" -c samples/basic/minimal-config-wsdl.json -h api-env -u apiadmin -p changeme`

Creates a SOAP-Service API having the API-Definition internally:  
`scripts\api-import.bat -c samples/basic/minimal-config-wsdl-api-definition.json -h api-env -u apiadmin -p changeme`  

### Inbound Security
Expose the API using an API-Key:  
`scripts\api-import.bat -a ../petstore.json -c samples/securityInbound/security-apikey-config.json -h api-env -u apiadmin -p changeme`

Securing the API using OAuth (API-Manager is the Authorization-Server):  
`scripts\api-import.bat -a ../petstore.json -c samples/securityInbound/security-oauth-config.json -h api-env -u apiadmin -p changeme`

Securing the API using OAuth-External (using an external Token-Provider):  
`scripts\api-import.bat -a ../petstore.json -c samples/securityInbound/security-oauth-external-config.json -h api-env -u apiadmin -p changeme`
In order to run this sample a token information policy named: `Tokeninfo policy` is required.

Custom-Policy for Frontend API-Security:  
`scripts\api-import.bat -a ../petstore.json -c samples/securityInbound/security-custom-policy-config.json -h api-env -u apiadmin -p changeme`
In order to run this sample a Custom-Authentication-Policy named: `Custom authentication policy` is required.


Two-way-SSL:  
__Please note__: Not yet supported by Swagger-Promote

### Outbound Authentication
Using an API-Key:  
`scripts\api-import.bat -a ../petstore.json -c samples/securityOutbound/security-outbound-apikey.json -h api-env -u apiadmin -p changeme`

Using HTTP-Basic:  
`scripts\api-import.bat -a ../petstore.json -c samples/securityOutbound/security-outbound-basic.json -h api-env -u apiadmin -p changeme`

Using Client-Certificate:  
`scripts\api-import.bat -a ../petstore.json -c samples/securityOutbound/security-outbound-clientCert.json -h api-env -u apiadmin -p changeme`


### Custom Policies
In order to run the following samples, custom policies must be registered to the API-Manager.  
Sample Request Policy 1, Sample Response Policy 1, Sample Routing Policy 1, Sample Fault-Handler Policy 1  

Using all possible Outbound Policies (Request, Routing, Response, Fault-Handler):  
`scripts\api-import.bat -a ../petstore.json -c samples/customPolicies/all-policies.json -h api-env -u apiadmin -p changeme`

Request policy only:  
`scripts\api-import.bat -a ../petstore.json -c samples/customPolicies/request-policy.json -h api-env -u apiadmin -p changeme`

Routing policy only:  
`scripts\api-import.bat -a ../petstore.json -c samples/customPolicies/routing-policy.json -h api-env -u apiadmin -p changeme`

### Manage Organizations and Apps
Grant access to an API for ALL organizations:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/grant-access-to-all-orgs-config.json -h api-env -u apiadmin -p changeme`

Grant access to an API to some organizations:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/grant-access-to-some-orgs-config.json -h api-env -u apiadmin -p changeme`
This sample is expecting the organizations: `Another org` and `My Partner` to exists.  

Automatically subscribe applications identified by the App-Name:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/apps-using-name-config.json -h api-env -u apiadmin -p changeme`
This sample is expecting applications names: `Client App 1` and `Client App 2` to exists.  

Automatically subscribe applications identified by the API-Key:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/apps-using-apiKey-config.json -h api-env -u apiadmin -p changeme`
This sample is expecting an application with API-Key: `App with API-Key XYZ` to exists.  

Automatically subscribe applications identified by the External Client-ID:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/apps-using-extClientId-config.json -h api-env -u apiadmin -p changeme`
This sample is expecting an application with External Client-ID: `App with external Client-ID ABC` to exists.  

Automatically subscribe applications identified by the internal OAuth Client-ID:  
`scripts\api-import.bat -a ../petstore.json -c samples/orgsAndApps/apps-using-oauthClientId-config.json -h api-env -u apiadmin -p changeme`
This sample is expecting an application with OAuth-Client-ID: `App with Client-ID ABC` to exists.

### Configure an API including quotas
The following API will have only 1 System-Quota:  
`scripts\api-import.bat -a ../petstore.json -c samples/quota/system-quota-only.json -h api-env -u apiadmin -p changeme`

Configure a number of System- and Application-Quota for this API:  
`scripts\api-import.bat -a ../petstore.json -c samples/quota/multi-restriction-quota.json -h api-env -u apiadmin -p changeme`

Replace all existing quotas with the following:  
`scripts\api-import.bat -a ../petstore.json -c samples/quota/multi-restriction-quota.json -h api-env -u apiadmin -p changeme -quotaMode=replace`

### Some complex examples
Manages an API:  
- secured with API-Key  
- VHost is set  
- overrides the BackendBasePath  
- adds an image  
- is using Custom-Policies (must be configured in API-Manager)   
- an API-Key for Outbound Authentication  
- is adding some Tags  
- a Default-CORS-Profile is configured  
- Inbound-Certificates are registered  
- System- and Application-Default-Quota is managed   
- Custom-Properties are declared (must be configured in API-Manager)  
`scripts\api-import.bat -a ../petstore.json -c samples/complex/complete-config.json -h api-env -u apiadmin -p changeme`

### Method-Level overrides
Configures API-Key + CORS on one method - All other methods are using PassThrough:  
`scripts\api-import.bat -a ../petstore.json -c samples/methodLevel/api-key-and-cors-one-method.json -h api-env -u apiadmin -p changeme`

Configures API-Key + CORS on __two__ methods - All other methods are using PassThrough:  
`scripts\api-import.bat -a ../petstore.json -c samples/methodLevel/api-key-and-cors-two-methods.json -h api-env -u apiadmin -p changeme`

Using OAuth as default and specific API-Key on only one API-Method:  
`scripts\api-import.bat -a ../petstore.json -c samples/methodLevel/oauth-default-apikey-on-one-method.json -h api-env -u apiadmin -p changeme`

Configure Outbound API-Method override for one method, using a special Authentication-Profile and adding an additional parameter:
`scripts\api-import.bat -a ../petstore.json -c samples/methodLevel/outboundbound-httpbasic-add-param.json -h api-env -u apiadmin -p changeme`  

### Staging examples
Is loading a Environment-Properties: env.api-env.properties and overrides API-Config with minimal-config-api-definition.api-env.json:
`scripts\api-import.bat -c samples/staging/minimal-config-api-definition.json -s api-env`

Is overriding the default API-Config: minimal-config-api-definition.json with minimal-config-api-definition.prod.json (No prod specific Env-File is used):
`scripts\api-import.bat -c samples/staging/minimal-config-api-definition.json -s prod -h api-env -u apiadmin -p changeme `
