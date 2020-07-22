# Axway API-Management CLI Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

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
