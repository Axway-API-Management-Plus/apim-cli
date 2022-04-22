# APIM-CLI Development information

This file contains information which is relevant for the development of the APIM CLI. 
For example, how the APIM CLI is released or integration tests work.

## Create a release

The version management concept is based on the develop and master branches. New features, 
bug fixes, etc. are created in appropriate branches and merged into the development branch when they 
are ready for the next version. This means that the develop branch is actually stable or buildable at all times.  
  
For the release, develop is merged into Master and the release is generated on Master with Maven.

__1. Merge develop into master__
  ```
  git checkout master
  git merge develop
  ```

__2. Make final changes before releasing__  
  ```sh
  # Modify the CHANGELOG.md and set the release date and version number
  git commit
  git push
  ```

__3. Create Pre-Release with Maven__  
  ```sh
  mvn -Darguments=-DskipTests release:prepare -P release
  
  # Set the version number following [Semantic Versioning](https://semver.org/)
  # Git-Label version should be for example: 1.11.0
  ```

__4. Validate the Pre-Release__  
  - Use the Pre-Release to manually perform some smoke tests  
  `apim-cli\modules\distribution\target`
    - If you are not happy with the actual build for any reason, you need to rollback the actual release prepare:  
    ```sh
    mvn release:rollback
    # Sometimes the created tag isn't removed automatically. You need to delete it to re-execute release:prepare: 
    git tag -d 1.11.0
    git push origin :refs/tags/1.11.0
    ```

__5. Create the release__  
  ```sh
  # This uploads the release artifacts to Maven-Central automatically
  mvn -Darguments=-DskipTests release:perform -P release
  ```
  
__6. Create a release on GitHub__  
  - Select the tag created by Maven
  - Use the description from the previous release and modify it
  - It also pushes the Chocolately package
  - upload the created release files to GitHub: 
    ```
    target/checkout/modules/distribution/target/axway-apimcli-1.11.0.zip
    target/checkout/modules/distribution/target/axway-apimcli-1.11.0.tar.gz
    ```
  
__8. Commit all changes and merge master into develop__
  ```
  git commit
  git push
  git checkout develop
  git merge master
  ```
