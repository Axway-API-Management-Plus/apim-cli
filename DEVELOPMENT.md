# APIM-CLI Development information

This file contains information which is relevant for the development of the APIM CLI. 
For example, how the APIM CLI is released or integration tests work.

## Create a release

The version management concept is based on the develop and master branches. New features, 
bug fixes, etc. are created in appropriate branches and merged into the development branch when they 
are ready for the next version. This means that the develop branch is actually stable or buildable at all times.  

### Prerequisites

- A Windows, Linux or Mac workstation with Git installed.  
- Write access on master and develop on https://github.com/Axway-API-Management-Plus/apim-cli
- A Sonatype user with access to https://s01.oss.sonatype.org
  - And access to project: `com.github.axway-api-management-plus`
  - Create a JIRA-Ticket to get access (https://central.sonatype.org/publish/publish-guide)
- GnuPG for Code-Signing (e.g. https://gnupg.org/)
- Apache Maven 3.6.3 or higher

### Maven pgp usage
- https://maven.apache.org/plugins/maven-gpg-plugin/usage.html


## Create a new release
  
For the release, develop is merged into Master and the release is generated on Master with Maven.

### 1. Checkout the APIM-CLI
```
git clone https://github.com/Axway-API-Management-Plus/apim-cli.git
```

### 2. Merge develop into master
```
git checkout master
git merge develop
```

### 3. Make final changes before releasing

```sh
vi CHANGELOG.md # set the release date and version number
git commit
git push
```

### 4. Create Release with Maven 

- Run Github action Release API CLI on github and Maven repository

  
### 5. Commit all changes and merge master into develop
```
git checkout develop
git merge master
git push
```
