## API-Manager Swagger-Promote examples

This directory contains a number of examples, which can be executed out of the box and configuration fragments can taken 
over into your API-Configuration.  

All examples will be grouped into different sections to make it easier to find them.  

### Basic Use-cases
Creates the most simple API, without any special settings:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/basic/minimal-config.json -s api-env
```

Instead of providing the API externally, it's referenced as part of the API-Config:  
```
scripts\apim.bat api import -c samples/basic/minimal-config-api-specification.json -s api-env
```

Creates a SOAP-Service API:    
```
scripts\apim.bat api import -a "http://www.dneonline.com/calculator.asmx?wsdl" -c samples/basic/minimal-config-wsdl.json -s api-env
```

Creates a SOAP-Service API having the API-Definition internally:  
```
scripts\apim.bat api import -c samples/basic/minimal-config-wsdl-api-specification.json -s api-env
```

### Inbound Security
Expose the API using an API-Key:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityInbound/security-apikey-config.json -s api-env
```

Securing the API using OAuth (API-Manager is the Authorization-Server):  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityInbound/security-oauth-config.json -s api-env
```

Securing the API using OAuth-External (using an external Token-Provider):  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityInbound/security-oauth-external-config.json -s api-env
```
In order to run this sample a token information policy named: `Tokeninfo policy` is required.

Custom-Policy for Frontend API-Security:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityInbound/security-custom-policy-config.json -s api-env
```
In order to run this sample a Custom-Authentication-Policy named: `Custom authentication policy` is required.


Two-way-SSL:  
__Please note__: Not yet supported by Swagger-Promote

### Outbound Authentication
Using an API-Key:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityOutbound/security-outbound-apikey.json -s api-env -u apiadmin -p changeme
```

Using HTTP-Basic:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityOutbound/security-outbound-basic.json -s api-env
```

Using Client-Certificate:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/securityOutbound/security-outbound-clientCert.json -s api-env
```


### Custom Policies
In order to run the following samples, custom policies must be registered to the API-Manager.  
Sample Request Policy 1, Sample Response Policy 1, Sample Routing Policy 1, Sample Fault-Handler Policy 1  

Using all possible Outbound Policies (Request, Routing, Response, Fault-Handler):  
```
scripts\apim.bat api import -a ../petstore.json -c samples/customPolicies/all-policies.json -s api-env
```

Request policy only:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/customPolicies/request-policy.json -s api-env
```

Routing policy only:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/customPolicies/routing-policy.json -s api-env
```

### Manage Organizations and Apps
Grant access to an API for ALL organizations:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/grant-access-to-all-orgs-config.json -s api-env
```

Grant access to an API to some organizations:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/grant-access-to-some-orgs-config.json -s api-env
```
This sample is expecting the organizations: `Another org` and `My Partner` to exists.  

Automatically subscribe applications identified by the App-Name:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/apps-using-name-config.json -s api-env
```
This sample is expecting applications names: `Client App 1` and `Client App 2` to exists.  

Automatically subscribe applications identified by the API-Key:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/apps-using-apiKey-config.json -s api-env
```
This sample is expecting an application with API-Key: `App with API-Key XYZ` to exists.  

Automatically subscribe applications identified by the External Client-ID:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/apps-using-extClientId-config.json -s api-env
```
This sample is expecting an application with External Client-ID: `App with external Client-ID ABC` to exists.  

Automatically subscribe applications identified by the internal OAuth Client-ID:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/orgsAndApps/apps-using-oauthClientId-config.json -s api-env
```
This sample is expecting an application with OAuth-Client-ID: `App with Client-ID ABC` to exists.

### Configure an API including quotas
The following API will have only 1 System-Quota:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/quota/system-quota-only.json -s api-env
```

Configure a number of System- and Application-Quota for this API:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/quota/multi-restriction-quota.json -s api-env
```

Replace all existing quotas with the following:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/quota/multi-restriction-quota.json -s api-env -quotaMode=replace
```

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
```
scripts\apim.bat api import -a ../petstore.json -c samples/complex/complete-config.json -s api-env
```

### Method-Level overrides
Configures API-Key + CORS on one method - All other methods are using PassThrough:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/methodLevel/api-key-and-cors-one-method.json -s api-env
```

Configures API-Key + CORS on __two__ methods - All other methods are using PassThrough:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/methodLevel/api-key-and-cors-two-methods.json -s api-env
```

Using OAuth as default and specific API-Key on only one API-Method:  
```
scripts\apim.bat api import -a ../petstore.json -c samples/methodLevel/oauth-default-apikey-on-one-method.json -s api-env
```

Configure Outbound API-Method override for one method, using a special Authentication-Profile and adding an additional parameter:
```
scripts\apim.bat api import -a ../petstore.json -c samples/methodLevel/outboundbound-httpbasic-add-param.json -s api-env
```  

### Staging examples
Is loading a Environment-Properties: env.api-env.properties and overrides API-Config with minimal-config-api-specification.api-env.json:
```
scripts\apim.bat api import -c samples/staging/minimal-config-api-specification.json -s api-env
```

Is overriding the default API-Config: minimal-config-api-specification.json with minimal-config-api-specification.prod.json (No prod specific Env-File is used):
```
scripts\apim.bat api import -c samples/staging/minimal-config-api-specification.json -s prod -s api-env
```
