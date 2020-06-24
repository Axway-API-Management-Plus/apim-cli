# Axway API-Management CLI Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- Feature to use a YAML based API-Definition for Swagger & OpenAPI (API-Manager 7.7 only)
- API Backend-Server information in wide and ultra view console get view
- Option to filter API having a specificied backendBasepath
- Show detailed API information if only 1 API is returned (See issue [#11](https://github.com/Axway-API-Management-Plus/apim-cli/issues/11))
- Feature to load the API-Definition from the FE-API (See issue [#4](https://github.com/Axway-API-Management-Plus/apim-cli/issues/4))
- Feature to export APIs as a CSV-File incl. used polices and subscribed applications
- CLI usage information for all operations improved

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
