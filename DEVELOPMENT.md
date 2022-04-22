# APIM-CLI Development information

This file contains information which is relevant for the development of the APIM CLI. 
For example, how the APIM CLI is released or integration tests work.

## Create a release

The version management concept is based on the develop and master branches. New features, 
bug fixes, etc. are created in appropriate branches and merged into the development branch when they 
are ready for the next version. This means that the develop branch is actually stable or buildable at all times.  
  
For the release, develop is merged into Master and the release is generated on Master with Maven.

1. Merge develop into master
  - `git checkout master`
  - `git merge develop`
2. Make final changes before releasing
  - adjust the CHANGELOG.md to set the version number
  - `git commit`
  - `git push`
3. Create Pre-Release with Maven
  - `mvn -Darguments=-DskipTests release:prepare -P release`
  - Set the version number following [Semantic Versioning](https://semver.org/)
  - the Git-Label version should be for example: 1.11.0
4. Validate the Pre-Release
  - Use the created release package to manually perform some smoke tests
    - `apim-cli\modules\distribution\target`
  - If you are not happy with the actual build for any reason, you need to rollback the actual release prepare: 
    - `mvn release:rollback`
  - Quite often the created tag isn't removed automatically. You need to delete it to re-execute release:prepare:
    - `git tag -d 1.11.0`
	- `git push origin :refs/tags/1.11.0`
5. Create the release
  - `mvn -Darguments=-DskipTests release:perform -P release`
  - This automatically uploads the release artifacts to Maven-Central
6. Create a release on GitHub
  - based on the create tag
  - use the description from the previous release and modify it
  - upload the created release files to GitHub: 
    - `axway-apimcli-1.11.0.zip`
	- `axway-apimcli-1.11.0.tar.gz`
7. Create the Chocolatey package
  - Copy 
    - `target/checkout/modules/distribution/target/axway-apimcli-1.11.0.zip`
	- to
	- `tools/choco/axway-apim-cli/tools`
  - Adjust the version number in: 
    - `tools/choco/axway-apim-cli/axway-apim-cli.nuspec`
  - Create the package
    - `choco pack`
  - Push the package
    - `choco push --api-key <api-key-goes-here> axway-apim-cli.1.11.0.nupkg`
8. Commit all changes and merge master into develop
  - `git commit`
  - `git push`
  - `git checkout develop`
  - `git merge master`