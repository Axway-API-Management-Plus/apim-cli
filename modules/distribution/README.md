# Module to build a new Distribution package

With that, a new tar.gz package is created and the Swagger-Promote core is deployed to Maven-Central.

### 

### To create a new release use the following instructions:
1. When the develop branch is stable and a new release should be created, merge everything from develop into master branch (e.g. using Pull-Request)  
2. Checkout the master branch  
3. Open the main project directory  
`cd C:\workspaces\api-management\apimanager-swagger-promote`
4. Validate a clean state on master (no uncommited changes)  
`git branch -avv`
6. Prepare the release  
With that, 
Using the profile: release in order to sign the code and upload it to Nexus  
Skip the tests, as they have been automatically executed already on Travis  
`mvn -Darguments=-DskipTests release:prepare -P release`  
7. Release it  
`mvn -Darguments=-DskipTests release:perform -P release`  
8. Upload the Distribution package to the release on Github  
