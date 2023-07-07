# Axway API-Management CLI Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.14.0] In progress
### Fixed
- Inbound Security - Query String Pass Through is not included in api get (See issue [#392](https://github.com/Axway-API-Management-Plus/apim-cli/issues/392))
- breaking changes with overrideSpecBasePath = true (See issue [#397](https://github.com/Axway-API-Management-Plus/apim-cli/issues/397))
### Added
- Add an option to output the json config file in console(See issue [#322](https://github.com/Axway-API-Management-Plus/apim-cli/issues/322))
    - If settings are imported from old versions to May 2023 release, set following values  to null as os and architecture elements are removed, (Refer [RDAPI-29419](https://docs.axway.com/bundle/axway-open-docs/page/docs/apim_relnotes/20230530_apimgr_relnotes/index.html))
    ```json
    {
      "os" : null,
      "architecture" : null
    }
    ```

## [1.13.7] 2023-04-21

### Fixed
- Add output for apim.sh api check-cert (See issue [#374](https://github.com/Axway-API-Management-Plus/apim-cli/issues/374))
- A change in application quota is not detected (See issue [#382](https://github.com/Axway-API-Management-Plus/apim-cli/issues/382))
- Snakeyaml size limitation (Nullpointer Exception when downloading API Spec via apim CLI) (See issue [#384](https://github.com/Axway-API-Management-Plus/apim-cli/issues/384))

## [1.13.6] 2023-04-03

### Fixed
- Command Line Option "zeroDowntimeUpdate" is not working (See issue [#370](https://github.com/Axway-API-Management-Plus/apim-cli/issues/370))
- Error updating existing app quota that has no pre-existing quota (See issue [#371](https://github.com/Axway-API-Management-Plus/apim-cli/issues/371))
- [APIExportApp] ERROR: An error happened during export. Please check the log (See issue [#376](https://github.com/Axway-API-Management-Plus/apim-cli/issues/376))

### Added
- Support of All / Global quotas for API and application. (See issue [#362](https://github.com/Axway-API-Management-Plus/apim-cli/issues/362))
- Host docker cli images on github docker registry [#373](https://github.com/Axway-API-Management-Plus/apim-cli/issues/373))


## [1.13.5] 2023-03-15

### Fixed
- apim does not import the whole chain in a PEM certificate (See issue [#361](https://github.com/Axway-API-Management-Plus/apim-cli/issues/361))
- Field BackendBasePath in api-config.json not used (See issue [#354](https://github.com/Axway-API-Management-Plus/apim-cli/issues/354))
- APIM-CLI Fails to export API WSDL Specifications starting with comments (See issue [#364](https://github.com/Axway-API-Management-Plus/apim-cli/issues/364))
- APIM-CLI - parsing valid metadata fails for OdataV4 api (See issue [#363](https://github.com/Axway-API-Management-Plus/apim-cli/issues/363))

### Added
- Introduced new parameter **overrideSpecBasePath** to fix issue [#354](https://github.com/Axway-API-Management-Plus/apim-cli/issues/354)
- Introduced new parameter **timeout** to handle read, connection and socket timout. The default value is 30 seconds.

## [1.13.4] 2023-02-28

### Fixed
- Retry-Delay does not appear in help text (See issue [#349](https://github.com/Axway-API-Management-Plus/apim-cli/issues/349))
- Dat file is not exported if Export API with an Organization that have a virtual host containing a ":" (See issue [#352](https://github.com/Axway-API-Management-Plus/apim-cli/issues/352))
- backendBasepath not working correctly when server url missing in swagger (See issue [#348](https://github.com/Axway-API-Management-Plus/apim-cli/issues/348))

### Added
- Support yaml configuration for apim cli (Beta)
- Support for APIM February 2023 release [#346](https://github.com/Axway-API-Management-Plus/apim-cli/issues/346))


## [1.13.3] 2023-02-08

### Fixed
- Special characters in Policy name used request, response or routing policy of an API (See issue [#336](https://github.com/Axway-API-Management-Plus/apim-cli/issues/336))
- Export of API created manually in API Manager fails with apim-cli (See issue [#337](https://github.com/Axway-API-Management-Plus/apim-cli/issues/337))
- Support API Gateway environment variable (${env.backend}) in Backend base path (See issue [#332](https://github.com/Axway-API-Management-Plus/apim-cli/issues/332))
- BackendBasepath not handled properly for open api specification (See issue [#341](https://github.com/Axway-API-Management-Plus/apim-cli/issues/341))

## Added

- APIM Multiple Organization support
- Sonar cloud code coverage -  [SonarCloud](https://sonarcloud.io/summary/new_code?id=Axway-API-Management-Plus_apim-cli). 

## Removed
- APIM 7.6.2 support
- Parameters ignoreAdminAccount and allowOrgAdminsToPublish in favor of multi organization support
- Parameter replaceHostInSwagger. 

## [1.13.2] 2022-12-13

### Fixed

- Export apiRoutingKey (See issue [#323](https://github.com/Axway-API-Management-Plus/apim-cli/issues/323))
- apim-cli behaviour during restoration of 2 identical apis but exposed on two different virtual host  (See issue [#331](https://github.com/Axway-API-Management-Plus/apim-cli/issues/331))
- APIM UI FeAPI via UI WSDL import, exported and imported by CLI fails, due to wsdl file reference api-config.json (See issue [#328](https://github.com/Axway-API-Management-Plus/apim-cli/issues/328))
    - Added an environment variable with value **retain.backend.url=true** to add URL used to import WSDL to API manger to api-config.json instead of writing WSDL to file system.
- BUILD SUCCESSFUL despite errors during pipeline execution for publish and un-publish operations (See issue [#330](https://github.com/Axway-API-Management-Plus/apim-cli/issues/330))
- Upgraded jackson, ascii-table and log4j jars to latest versions

### Added
- Support for November 2022 APIM release (7.7-20221130)



## [1.13.1] 2022-11-15 

### Fixed
- Get API causes an error when related certificates contains slash character '/' in their CN (See issue [#315](https://github.com/Axway-API-Management-Plus/apim-cli/issues/315))
- globalrequest and response polices are not handled correctly using apim settings import (See issue [#325](https://github.com/Axway-API-Management-Plus/apim-cli/issues/325))
- Add support for OData V4 specifications  (See issue [#234](https://github.com/Axway-API-Management-Plus/apim-cli/issues/234))
- Security fix - Bump commons-text from 1.9 to 1.10.0
- Security fix Bump jackson-databind from 2.13.3 to 2.13.4.1

### Changed
- Cleaned up some System.out.println to reduce console output

### Added
- command "api check-certs -days 90 -s api-env -o json" command writes json output on console

## [1.13.0] 2022-09-23

### Fixed
- Junit fix (See issue [#305](https://github.com/Axway-API-Management-Plus/apim-cli/issues/305))
- Attempt to import WSDL API definition produces HTTP error code 400 when creating front end API (See issue [#308](https://github.com/Axway-API-Management-Plus/apim-cli/issues/308))
- AXWAY_APIM_CLI_HOME in the environment may break tests (See issue [#306](https://github.com/Axway-API-Management-Plus/apim-cli/issues/306))
- Allow override per method for tags (See issue [#162](https://github.com/Axway-API-Management-Plus/apim-cli/issues/162))
- apim cli always use first org for api deployment if user is assigned to multiple org (See issue [#311](https://github.com/Axway-API-Management-Plus/apim-cli/issues/311))

### Added
- Automate APIM CLI config file creation based on openapi specification
- Support for release 7.7-20220830

## [1.12.3] 2022-08-09

### Fixed

- oas 3.0 base path handling (See issue [#297](https://github.com/Axway-API-Management-Plus/apim-cli/issues/297))
- BackendBasepath problem exporting SOAP API with apim-cli (See issue [#299](https://github.com/Axway-API-Management-Plus/apim-cli/issues/299))
- Importing SOAP API with apim-cli adds "+" instead of spaces for the Backend API name (See issue [#301](https://github.com/Axway-API-Management-Plus/apim-cli/issues/301))

### Changed
- Added new parameter disableCompression.  The parameter enables logging API Gateway responses for debugging.
  - Usage - **api get -u apiadmin -p Space*Salt*25 -h 208.67.129.25 -port 8075 -n "PetStore 3.0" -o json -disableCompression**
  - Enable http client header and payload logging using jvm -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
- If backendBasepath is present in api-config.json, Openapi (OAS 3) servers.url will be modified based on the values. E.g
  - If open api servers.url is /api/v3 and backendBasepath is https://backend, the openapi server.url will be replaced with https://backend/api/v3
  - If open api servers.url is https://petstore.swagger.io/api/v3 and backendBasepath is https://backend, the openapi server.url will be replaced with https://backend/api/v3
  - If open api servers.url is https://petstore.swagger.io/api/v3 and backendBasepath is https://backend/api, the openapi server.url will be replaced with https://backend/api/api/v3

## [1.12.2] 2022-07-28

### Fixed
- APIManagerAPIAdapter keeps old parameters (See issue [#293](https://github.com/Axway-API-Management-Plus/apim-cli/issues/293))
- Proxy Authentication is not working (See issue [#295](https://github.com/Axway-API-Management-Plus/apim-cli/issues/295))
- JsonAPIExporter.saveAPILocally logs Exception, but APIExportApp.exportAPI returns 0 (See issue [#298](https://github.com/Axway-API-Management-Plus/apim-cli/issues/298))

### Changed
- Sample Dockerfile for apimcli
- Environmentalize log level in log42.xml with environment variable LOG_LEVEL. Default LOG_LEVEL is info

## [1.12.1] 2022-07-06

### Fixed
- Configured API access of an organization not updated (See issue [#290](https://github.com/Axway-API-Management-Plus/apim-cli/issues/290))

## [1.12.0] 2022-06-09

### Added
- New parameter apimanagerUrl added, which can be used instead of host and port. 
- Support for release 7.7-20220530
- Support for API-Manager feature flag api.manager.orgadmin.selfservice.enabled (See issue [#217](https://github.com/Axway-API-Management-Plus/apim-cli/issues/217))

### Fixed
- APIM-CLI was not able to connect to API-Manager running on GKE using ingress class: gce-internal due to the port 443 part of the host header
- Logging improved for failed REST-API requests
- Changing the application organization was not recognized as a change (See issue [#284](https://github.com/Axway-API-Management-Plus/apim-cli/issues/284))

### Changed
- Dropped support for release 7.7-20200130
- Now using API-Manager REST-API version /api/portal/v1.4
- Updated libs
	- jackson-* from 2.13.2 to 2.13.3
	- olingo-odata2-api from 2.0.11 to 2.0.12
	- log4j-slf4j-impl from 2.17.1 to 2.17.2
	- ascii-table from 1.1.0 to 1.2.0
- Changed the way the APIM-CLI install folder is located (See issue [#276](https://github.com/Axway-API-Management-Plus/apim-cli/issues/276))

## [1.11.0] 2022-04-22

### Fixed
- Now underlying cache is based on API-Manager Hostname and Port (See issue [#265](https://github.com/Axway-API-Management-Plus/apim-cli/issues/265))
- Problem on filtering API with policy name (See issue [#268](https://github.com/Axway-API-Management-Plus/apim-cli/issues/268))
- Application export always returned error code 0 (See issue [#278](https://github.com/Axway-API-Management-Plus/apim-cli/issues/278))
- Password protected WSDL Specifications dont work (See issue [#277](https://github.com/Axway-API-Management-Plus/apim-cli/issues/277))

### Changed
- Now, when using toggle: -useFEAPIDefinition it is no longer using swaggerVersion 2.0 by default, instead it tries to download the spec using version 3.0, 2.0 and 1.1
### Changed
- Updated jackson-* from 2.13.0 to 2.13.2
- Updated commons-csv from 1.8 to 1.9.0
- Updated swagger-models from 2.1.11 to 2.2.0

### Security
- Updated Jackson Databind from version 2.13.0 to 2.13.2.1 to fix CWE-787

## [1.10.1] 2022-02-25

### Fixed
- API-Import fails when using Method-Level quotas (See issue [#261](https://github.com/Axway-API-Management-Plus/apim-cli/issues/261))
- Export API to JSON fails if V-Host of API contains a port (See issue [#262](https://github.com/Axway-API-Management-Plus/apim-cli/issues/262))
- Exported quota methods now contain the method-name (operationId) instead of the internal ID
- try again the REST-API call on the quota endpoint if the response is API found
  - this is required for the API-Management release 7.7.0.20220228

## [1.10.0] 2022-02-17

### Added
- Support to provide a list of local markdown description files that can be combined with the original description

### Fixed
- Now the given parameter -port is trimmed to avoid a number format exception

## [1.9.0] 2022-02-09

### Added
- Support to filter API-Specifications Models/Schemas

### Changed
- Include/Exclude filter structure changed from single object to an array of filters

## [1.8.0] 2022-02-01

### Added
- Support to filter API-Specifications based on the Paths or Tags (See issue [#238](https://github.com/Axway-API-Management-Plus/apim-cli/issues/238))
- Support to filter for the creator of an application (See issue [#252](https://github.com/Axway-API-Management-Plus/apim-cli/issues/252))
- CSV- and Application-Console export now contains the creator of an application (See issue [#252](https://github.com/Axway-API-Management-Plus/apim-cli/issues/252)) 

### Fixed
- Environment-Variables in API-config file gets not substituted anymore (See issue [#257](https://github.com/Axway-API-Management-Plus/apim-cli/issues/257))

## [1.7.1] 2022-01-20

### Fixed
- Backend service URL not configured for SOAP-Services (See issue [#255](https://github.com/Axway-API-Management-Plus/apim-cli/issues/255))

## [1.7.0] 2022-01-14

### Changed
- Renamed log4j.xml into log4j2.xml
- Customer specific Cache-Configuration now read from conf/cacheConfig.xml

### Security
- Updated Apache Log4J from version 2.17.0 to 2.17.1

### Fixed
- App-Export - AppQuota contains API-ID instead of API-Name (See issue [#145](https://github.com/Axway-API-Management-Plus/apim-cli/issues/145))
- Outbound-AuthN OAuth-Provider-Profile only translated into FED-Name for _default profile (See issue [#246](https://github.com/Axway-API-Management-Plus/apim-cli/issues/246))
- Backslashes in user passwords are ignored (See issue [#244](https://github.com/Axway-API-Management-Plus/apim-cli/issues/244))

### Added
- Support to use a cache for import actions (See issue [#253](https://github.com/Axway-API-Management-Plus/apim-cli/issues/253))
- UpdateOnly toggle (See issue [#251](https://github.com/Axway-API-Management-Plus/apim-cli/issues/251))
- Support to delete applications from API-Manager
- Support for API-Method level quotas

## [1.6.1] 2021-12-20

### Security
- Updated Apache Log4J from version 2.16.0 to 2.17.0 

## [1.6.0] 2021-12-17

### Added
- Logging improved if APIM-CLI is running in batch mode

### Security
- Updated Apache Log4J from version 2.15.0 to 2.16.0 (See issue [#225](https://github.com/Axway-API-Management-Plus/apim-cli/issues/225))

### Changed
- Updated commons-cli from 1.4 to 1.5.0
- Updated commons-io from 2.8.0 to 2.11.0
- Updated commons-lang3 from 3.11 to 3.12.0
- Updated commons-text from 1.8 to 1.9
- Updated jackson-* from 2.11.3 to 2.13.0
- Updated ehcache from 3.8.1 to 3.9.7 (requires to add dependency: org.glassfish.jaxb:jaxb-runtime:2.3.5)

## [1.5.1] 2021-12-13
### Security
- Updated Apache Log4J from version 1.2.17 to 2.15.0

## [1.5.0] 2021-11-11
### Fixed
- API-Manager Remote-Hosts export not working - Filtered incorrectly (See issue [#225](https://github.com/Axway-API-Management-Plus/apim-cli/issues/225))

### Added
- Added support for OData V2 EDMX specifications (See issue [#228](https://github.com/Axway-API-Management-Plus/apim-cli/issues/228))
- Add check expired certificates command (See issue [#233](https://github.com/Axway-API-Management-Plus/apim-cli/issues/233))
- Control application user permissions (See issue [#186](https://github.com/Axway-API-Management-Plus/apim-cli/issues/186))
- Entity import should fail if a required custom property is missing (See issue [#229](https://github.com/Axway-API-Management-Plus/apim-cli/issues/229))
- APIM-CLI should check & fail, when RouteType is Policy without providing a RoutingPolicy (See issue [#227](https://github.com/Axway-API-Management-Plus/apim-cli/issues/227))
- Improved logging for missing policies. Now the type of the missing policy is logged.

## [1.4.0] 2021-10-08
### Fixed
- No change detected when updating the role of a user (See issue [#219](https://github.com/Axway-API-Management-Plus/apim-cli/issues/219))
- API-Import with Method-Level Outbound-Profiles failes if no custom Outbound-Authentication is given (See issue [#220](https://github.com/Axway-API-Management-Plus/apim-cli/issues/220))
- Outbound SSL-Authentication is not working (See issue [#221](https://github.com/Axway-API-Management-Plus/apim-cli/issues/221))

### Added
- WADL support (See issue [#222](https://github.com/Axway-API-Management-Plus/apim-cli/issues/222))

### Changed
- Dropped support for API-Manager version 7.6.2

## [1.3.13] 2021-09-21
### Fixed
- Error upgrading access to newer API - The entity could not be found. (See issue [#218](https://github.com/Axway-API-Management-Plus/apim-cli/issues/218))

## [1.3.12] 2021-09-10
### Added
- Provide option to configure the Retry-Delay for some of the API-Manager REST-API calls (See issue [#213](https://github.com/Axway-API-Management-Plus/apim-cli/issues/213))

## [1.3.11] 2021-09-06
### Added
- Feature to grant API access for organization during import (See issue [#196](https://github.com/Axway-API-Management-Plus/apim-cli/issues/196))
- Feature to manually define the stage configuration file (See issue [#195](https://github.com/Axway-API-Management-Plus/apim-cli/issues/195))
- User-Import now supports staging and variables as the other entity types
- Feature to filter APIs with `-createdOn` based on their creation date. (See issue [#209](https://github.com/Axway-API-Management-Plus/apim-cli/issues/209))
- The API-Creation date is now exported into the CSV- and Console-Format (See issue [#208](https://github.com/Axway-API-Management-Plus/apim-cli/issues/208))

### Fixed
- Changed the Log-Threshold to DEBUG for Console-Logger to make debugging working according to the documentation
- Application-JSON-Export - Credentials should not contain ApplicationID (See issue [#146](https://github.com/Axway-API-Management-Plus/apim-cli/issues/146))
- Import Application scopes doesn't work (See issue [#206](https://github.com/Axway-API-Management-Plus/apim-cli/issues/206))

## [1.3.10] 2021-07-16
### Fixed
- No change detected when changing authenticationProfile in outboundProfiles (See issue [#184](https://github.com/Axway-API-Management-Plus/apim-cli/issues/184)) by @ftriolet
- A WSDL file with a long comment at the beginning is not recognized as a WSDL specification. (See issue [#190](https://github.com/Axway-API-Management-Plus/apim-cli/issues/190))

### Added
- Added feature to export APIs as DAT-Files (See issue [#191](https://github.com/Axway-API-Management-Plus/apim-cli/issues/191))

## [1.3.9] 2021-07-02
### Fixed
- Update RemoteHost lost CreatedBy & CreatedOn (See issue [#180](https://github.com/Axway-API-Management-Plus/apim-cli/issues/180))
- Manually configured API-Quotas get lost, when API is re-created (See issue [#187](https://github.com/Axway-API-Management-Plus/apim-cli/issues/187))
- Cannot delete API with an image, when the image is changed during deletion (See issue [#188](https://github.com/Axway-API-Management-Plus/apim-cli/issues/188))

## [1.3.8] 2021-06-17
### Fixed
- CLI does not realize changed policy when updating an API (See issue [#179](https://github.com/Axway-API-Management-Plus/apim-cli/issues/179))
- API-ManagerApps Adpater now handles 404 if searching for an Application based on the ID that does not exists (See issue [#168](https://github.com/Axway-API-Management-Plus/apim-cli/issues/168))
- vhost="" no longer creates a new API each time (See issue [#169](https://github.com/Axway-API-Management-Plus/apim-cli/issues/169))
- `apim api publish` resets API VHost to configured Default virtual host (See issue [#170](https://github.com/Axway-API-Management-Plus/apim-cli/issues/170))
- Malformed URL for Backend-URL no longer causing the CLI to fail (See issue [#175](https://github.com/Axway-API-Management-Plus/apim-cli/issues/175))
- Application import with no changes no longer logs an exception (See issue [#176](https://github.com/Axway-API-Management-Plus/apim-cli/issues/176))

### Added
- Trailing slash added to given backendBasepath (See issue [#178](https://github.com/Axway-API-Management-Plus/apim-cli/issues/178))

## [1.3.7] 2021-04-19
### Fixed
- apim.sh fails if CWD has a space (See issue [#160](https://github.com/Axway-API-Management-Plus/apim-cli/issues/160))
- Streams not closed bring to a FileSystemException (See issue [#161](https://github.com/Axway-API-Management-Plus/apim-cli/issues/161))
- Streams not closed bring to a FileSystemException (See issue [#165](https://github.com/Axway-API-Management-Plus/apim-cli/issues/165))
- Command grant access ignores given api path (See issue [#164](https://github.com/Axway-API-Management-Plus/apim-cli/issues/1654)
- Export-Error when using a Custom-Properties-Model for APIs/Apps/Orgs (See issue [#163](https://github.com/Axway-API-Management-Plus/apim-cli/issues/163))

### Added
- Resolve system environment variables in environment property files (See issue [#166](https://github.com/Axway-API-Management-Plus/apim-cli/issues/166))
- Support for API-Management 7.7-2021-March release (See issue [#167](https://github.com/Axway-API-Management-Plus/apim-cli/issues/167))

## [1.3.6] 2021-03-16
### Fixed
- Avoid NPE if given OAuth-Provider Profile is invalid (See issue [#143](https://github.com/Axway-API-Management-Plus/apim-cli/issues/143))
- Actual API-Lookup when using additional criteria V-Host and QueryRoutingVersion (See issue [#151](https://github.com/Axway-API-Management-Plus/apim-cli/issues/151))

### Changed
- Retry request at API access endpoint also for return code 404 (See issue [#157](https://github.com/Axway-API-Management-Plus/apim-cli/issues/157))
- If the useFEAPIDefinition flag is set, then the exported API specification is updated with the backend API information (host, basePath, schemes). (See issue [#158](https://github.com/Axway-API-Management-Plus/apim-cli/issues/158))

### Added
- Exported API-Manager settings now include Null-Values for not set properties (See issue [#150](https://github.com/Axway-API-Management-Plus/apim-cli/issues/150))
- Now Login to API-Manager is considered as successul if Status-Code is between 200-299 or 301 (See issue [#148](https://github.com/Axway-API-Management-Plus/apim-cli/issues/148))
- New api grant-access command allowing to grant access to multiple APIs & Orgs with one command (See issue [#153](https://github.com/Axway-API-Management-Plus/apim-cli/issues/153))

## [1.3.5] 2020-11-29
### Added
- Support set the API-Manager REST-API basepath (See issue [#141](https://github.com/Axway-API-Management-Plus/apim-cli/issues/141))

## [1.3.4] 2020-11-27
### Fixed
- java.lang.ClassCastException when trying to change an API (See issue [#131](https://github.com/Axway-API-Management-Plus/apim-cli/issues/131))
- apim api publish command outputs misleading error messages (See issue [#134](https://github.com/Axway-API-Management-Plus/apim-cli/issues/134))
- Existing App-Credentials should be updated and not always replaced (See issue [#138](https://github.com/Axway-API-Management-Plus/apim-cli/issues/138))
- When publish an API using the APIM CLI, events logs generated have null values for client, app & org (See issue [#136](https://github.com/Axway-API-Management-Plus/apim-cli/issues/136))
- When publish an API using the APIM CLI, events logs generated have null values for client, app & org (See issue [#136](https://github.com/Axway-API-Management-Plus/apim-cli/issues/136))
- API export to csv fails if API-Version is missing (See issue [#130](https://github.com/Axway-API-Management-Plus/apim-cli/issues/130))
- Bug on api approve without providing retirement date, working on v 1.3.2 (See issue [#132](https://github.com/Axway-API-Management-Plus/apim-cli/issues/132))

### Changed
- Renamed command "api upgrade" to "api upgrade-access" (See issue [#139](https://github.com/Axway-API-Management-Plus/apim-cli/issues/139))

## [1.3.3] 2020-11-27
### Fixed
- New API-Manager 7.7-September release settings are ignored during import (See issue [#119](https://github.com/Axway-API-Management-Plus/apim-cli/issues/119))
- Policies not shown in console view if API is not using a Routing-Policy (See issue [#121](https://github.com/Axway-API-Management-Plus/apim-cli/issues/121))

### Added
- Support to upgrade one or multiple APIs (See issue [#120](https://github.com/Axway-API-Management-Plus/apim-cli/issues/120))

## [1.3.2] 2020-11-25
### Fixed
- Application-Subscription not restored, when API is Republished to be updated and no applications given the API-Config (See issue [#117](https://github.com/Axway-API-Management-Plus/apim-cli/issues/117))

## [1.3.1] 2020-11-24
### Fixed
- Created BE-API response was parsing the response wrong. This could lead to an issue, if the API contains a createdOn field. (See issue [#112](https://github.com/Axway-API-Management-Plus/apim-cli/issues/112))
- Unicode API-Name was not shown correctly in the Backend-API overview (See issue [#113](https://github.com/Axway-API-Management-Plus/apim-cli/issues/113))
- Application-Subscription not restored, when API is Republished to be updated (See issue [#114](https://github.com/Axway-API-Management-Plus/apim-cli/issues/114))
- Filters not taken into consideration when deleting orgs

### Added
- Support to use a proxy for the API-Manager communication (See issue [#109](https://github.com/Axway-API-Management-Plus/apim-cli/issues/109))

## [1.3.0] 2020-11-10
### Added
- Search for APIs based on configured Inbound- and Outbound-Security (See issue [#86](https://github.com/Axway-API-Management-Plus/apim-cli/issues/86))
- Added support for API-Manager Config/Alerts/Remote-Hosts (See issue [#68](https://github.com/Axway-API-Management-Plus/apim-cli/issues/68))
- APIs now updated without Re-Creating them if possible (See issue [#95](https://github.com/Axway-API-Management-Plus/apim-cli/issues/95))
- Support to manage the full Remote-Host introduced with the API-Manager September/2020 release
- Capability to filter APIs based on their organization
- New command to approve one or more APIs that are in pending state (See issue [#97](https://github.com/Axway-API-Management-Plus/apim-cli/issues/97))
- Scripts apim.sh and apim.bat now optionally use AXWAY_APIM_CLI_HOME to setup the classpath (See issue [#100](https://github.com/Axway-API-Management-Plus/apim-cli/issues/100))
- Staging support for applications, users and organizations
- Support for custom properties for applications, users and organizations (See issue [#93](https://github.com/Axway-API-Management-Plus/apim-cli/issues/93))
- Support to read the API-Description from  a local markdown file See issue [#110](https://github.com/Axway-API-Management-Plus/apim-cli/issues/110))

### Fixed
- Avoid NPE during API-Export if API-Custom-properties are not configured (See issue [#90](https://github.com/Axway-API-Management-Plus/apim-cli/issues/90))
- Disabled applications not created as disabled in API-Manager (See issue [#89](https://github.com/Axway-API-Management-Plus/apim-cli/issues/89))
- Support for special characters like an accent (See issue [#88](https://github.com/Axway-API-Management-Plus/apim-cli/issues/88))
- Organizations now validated based on given stage (See issue [#58](https://github.com/Axway-API-Management-Plus/apim-cli/issues/58))
- API-Import is now stopped if host and backendBasepath is missing (See issue [#53](https://github.com/Axway-API-Management-Plus/apim-cli/issues/53))
- Command-Line option with or without arguments can now be given interchangeable (See issue [#102](https://github.com/Axway-API-Management-Plus/apim-cli/issues/102))
- apim.bat now works with or without JAVA_HOME set (See issue [#105](https://github.com/Axway-API-Management-Plus/apim-cli/issues/105))

### Changed
- APIs no longer Re-Created if not needed (See issue [#95](https://github.com/Axway-API-Management-Plus/apim-cli/issues/95))
- CLI Options parser refactored to be more flexible and support future requirements (See PR [#103](https://github.com/Axway-API-Management-Plus/apim-cli/pull/103))
- Updated Commons-text from version 1.8 to version 1.9
- Updated Jackson-Databind from version 2.9.10.5 to 2.9.10.6
- Updated commons-lang3 from version 3.10 to 3.11
- Updated commons-io from version 2.7 to 2.8.0
- Updated org.apache.httpcomponents httpclient from version 4.5.12 to 4.5.13
- Updated Jackson from 2.9.10 to 2.11.3

## [1.2.3] 2020-09-24
### Added
- Search for APIs based on configured tags (See issue [#82](https://github.com/Axway-API-Management-Plus/apim-cli/issues/82))
- Error handling improved if custom properties configuration is invalid (Switch or Select without configured options)
- Added support to import application scopes (See PR [#77](https://github.com/Axway-API-Management-Plus/apim-cli/pull/77))
- Leverage "com.axway.apimanager.api.model.disable.confidential.fields" option (See PR [#71](https://github.com/Axway-API-Management-Plus/apim-cli/pull/71))
- Support for Stage-Config & Variable substitution for Orgs, Client-Apps and Users
- Support to export & import API-Manager configuration

### Fixed
- Changing an APIs fails due to a ClassCastException (See issue [#85](https://github.com/Axway-API-Management-Plus/apim-cli/issues/85))
- NullPointerException when retrieving APIs on console with mode ultra (See issue [#80](https://github.com/Axway-API-Management-Plus/apim-cli/issues/80))
- Quota- and Tag-Information always shown as false in console view, even if configured for an API (See issue [#81](https://github.com/Axway-API-Management-Plus/apim-cli/issues/81))
- Handling of ExtClient configuration fixed when importing applications (See PR [#78](https://github.com/Axway-API-Management-Plus/apim-cli/pull/78))
- Error when updating an organization (See issue [#73](https://github.com/Axway-API-Management-Plus/apim-cli/issues/73))
- NPE when updating backendBasepath in a Swagger not having a host configured (See issue [#72](https://github.com/Axway-API-Management-Plus/apim-cli/issues/72))

### Changed
- Internal parameter handling refactore to make it easier to integrate the CLI into other Java-Programs (See PR [#83](https://github.com/Axway-API-Management-Plus/apim-cli/pull/83))

## [1.2.2] 2020-09-09
### Added
- Added new CSV-Export for applications (See issue [#70](https://github.com/Axway-API-Management-Plus/apim-cli/issues/70))
- Toggle ignoreCache added to the usage (See issue [#52](https://github.com/Axway-API-Management-Plus/apim-cli/issues/52))
- Added new forceUpdate flag to force re-creation (See issue [#56](https://github.com/Axway-API-Management-Plus/apim-cli/issues/56))

### Fixed
- apim.sh now starts the CLI in the current directory (See issue [#60](https://github.com/Axway-API-Management-Plus/apim-cli/issues/60))
- ErrorHandling improved when API-Config file can't be found
- HTTP-Basic Outbound handling fixed, if no password is given
- CSRF-Token handling improved (See issue [#67](https://github.com/Axway-API-Management-Plus/apim-cli/issues/67))
- Error when updating organization (See issue [#69](https://github.com/Axway-API-Management-Plus/apim-cli/issues/69))

### Changed
- Added 7.7-July and 7.6.2-SP5 releases to the list of tested releases
- apim.sh & apim.bat script now support path with blanks. Thanks to [@cbrowet-axway](https://github.com/cbrowet-axway)  
- Handling NPE for Application credential search (See issue [#57](https://github.com/Axway-API-Management-Plus/apim-cli/issues/57))
- If FE-Security is a Custom-Policy, the name of the Custom-Policy is now exported (See issue [#61](https://github.com/Axway-API-Management-Plus/apim-cli/issues/61))

## [1.2.1] 2020-07-22
### Fixed
- Outbound Method-Configuration not working (See issue [#50](https://github.com/Axway-API-Management-Plus/apim-cli/issues/50))

## [1.2.0] 2020-07-17
### Added
- Support to import and export organizations
- Support to import and export users
- Support to publish APIs
- Support to change the backend URL of an API (See issue [#43](https://github.com/Axway-API-Management-Plus/apim-cli/issues/43))
- Support to search applications based on their credentials (API-Key & Client-ID) (See issue [#44](https://github.com/Axway-API-Management-Plus/apim-cli/issues/44))
- Support to search applications based on their OAuth Redirect-URL
- Stabilized API-Access handling (See issue [#35](https://github.com/Axway-API-Management-Plus/apim-cli/issues/35))
- Unkown API-Specifications are now handled gracefully and exported as text files

### Changed
- Library Apache httpclient Version: 4.5.6 to 4.5.12
- Library Apache commons-io Version: 2.6 to 2.7
- Library Apache commons-text Version: 1.7 to 1.8
- Library Apache commons-lang 2.6 to commons-lang3 3.10
- Library Apache httpmime Version: 4.5.6 to 4.5.12
- Library slf4j-log4j12 Version: 1.7.25 to 1.7.30
- Test library testng Version: 6.9.10 to 6.9.13.6

### Fixed
- Fixed NPE when API has no security configured (See issue [#46](https://github.com/Axway-API-Management-Plus/apim-cli/issues/46))
- apim.sh changed to improve support on Mac/Linux systems. Identified by [@cbrowet-axway](https://github.com/cbrowet-axway) (See issue [#42](https://github.com/Axway-API-Management-Plus/apim-cli/issues/42))  
- Improving support for OpenAPI 3.x spec having no servers declared (See issue [#40](https://github.com/Axway-API-Management-Plus/apim-cli/issues/40))
- API export with methods doesn't contain anymore the internal method-id (See issue [#39](https://github.com/Axway-API-Management-Plus/apim-cli/issues/39))
- CLI tool is now taking over RC given by each module (See issue [#33](https://github.com/Axway-API-Management-Plus/apim-cli/issues/33))
- Policies are now exported with their name instead of the internal ID

## [1.1.0] 2020-06-25
### Added
- Feature to use a YAML based API-Definition for Swagger & OpenAPI (API-Manager 7.7 only)
- API Backend-Server information in wide and ultra view console get view
- Option to filter API having a specified backendBasepath
- Show detailed API information if only 1 API is returned (See issue [#11](https://github.com/Axway-API-Management-Plus/apim-cli/issues/11))
- Feature to load the API-Definition from the FE-API (See issue [#4](https://github.com/Axway-API-Management-Plus/apim-cli/issues/4))
- Feature to export APIs as a CSV-File incl. used polices and subscribed applications
- CLI usage information for all operations improved
- CLI now properly logs out from API-Manager after the execution
- API Custom-Properties are exported into CSV and Console view

### Changed
- API Console ultra view not longer renders API-Tags in detail. Only indicated with True & False
- renamed parameter `-l / --localFolder`    TO  `-t / --target`
- renamed parameter `-df / --deleteFolder`  TO  `-deleteTarget`
- renamed parameter `-f / --format`         TO  `-o --output`

### Fixed
- Wrong info text during deletion of an unpublished API proxy (See issue [#25](https://github.com/Axway-API-Management-Plus/apim-cli/issues/25))

### Security
- Bump jackson-databind from 2.9.10.4 to 2.9.10.5

## [1.0.1] 2020-06-17
### Fixed
- Custom-Properties missing in API-Export (See issue [#13](https://github.com/Axway-API-Management-Plus/apim-cli/issues/13))
- NullPointerException during App export when using OrgAdmin (See issue [#16](https://github.com/Axway-API-Management-Plus/apim-cli/issues/16))
- NullPointerException with option -ultra when using OrgAdmin (See issue [#12](https://github.com/Axway-API-Management-Plus/apim-cli/issues/12))
- Wrong info text during update of an unpublished API proxy (See issue [#15](https://github.com/Axway-API-Management-Plus/apim-cli/issues/15))

## [1.0.0] 2020-06-15
### Added
- Initial version of the APIM-CLI
- Swagger-Promote completely refactored
- Migrated API-Import & Export to dedicated modules
- New modules to manage applications (Import and Export)
- Get APIs and Applications (Filtered, different formats)
- Delete APIs (Front and Backend)
- Publish APIs
- Introduced API-Management programming interface

### Fixed
- Cannot set Oauth authentication Profile using its name (See issue [#2](https://github.com/Axway-API-Management-Plus/apim-cli/issues/2))
