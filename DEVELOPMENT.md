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

#### Maven settings.xml

A server configuration for GitHub write access by Maven using your personal access token. 

```xml
    <server>
      <id>github</id>
      <username>cwiechmann@axway.com</username>
      <password>YOUR_GITHUB_PAT</password>
    </server>
```
The Code must be signed to be published to Maven-Central. A Signing-Key must be created and published:  
Learn more: https://central.sonatype.org/publish/requirements/gpg/  
  
Confguration required for the Sonatype communication:  
Learn more: https://github.com/chhh/sonatype-ossrh-parent/blob/master/publishing-to-maven-central.md  
  
A profile Sonatype servers used by Sonatype maven plugin.

```xml
    <server>
      <id>ossrh</id>
      <username>YOUR_OSSRH_USERNAME</username>
      <password>YOUR_OSSRH_PASSWORD</password>
    </server>
```

A profile for GPG which is used by the `maven-gpg-plugin` plugin.

```xml
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>axway</gpg.passphrase>
      </properties>
    </profile>
```
Learn more: https://maven.apache.org/plugins/maven-gpg-plugin/usage.html

### Create a new release
  
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

### 4. Create Pre-Release with Maven 

```sh
mvn -Darguments=-DskipTests release:prepare -P release

# Set the version number following [Semantic Versioning](https://semver.org/)
# Git-Label version should be for example: 1.11.0
# Next SNAPSHOT Version: 1.12.0-SNAPSHOT
```

### 5. Validate the Pre-Release

- Use the Pre-Release to manually perform some smoke tests  
`apim-cli\distribution\target`
  - If you are not happy with the actual build for any reason, you need to rollback the actual release prepare:  
  ```sh
  mvn release:rollback
  # Sometimes the created tag isn't removed automatically. You need to delete it to re-execute release:prepare: 
  git tag -d 1.11.0
  git push origin :refs/tags/1.11.0
  ```

### 6. Create the release

```sh
# This uploads the release artifacts to Maven-Central automatically
mvn -Darguments=-DskipTests release:perform -P release
```
  
### 7. Create a release on GitHub

- Select the tag created by Maven
- Use the description from the previous release and modify it
- It also pushes the Chocolately package
- upload the created release files to GitHub: 
  ```
  target/checkout/distribution/target/axway-apimcli-1.11.0.zip
  target/checkout/distribution/target/axway-apimcli-1.11.0.tar.gz
  ```
  
### 8. Commit all changes and merge master into develop
```
git checkout develop
git merge master
git push
```
