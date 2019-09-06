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
Goal of this step is: 
- before doing this, make sure all sources are checked-in  
- tag the sources with the release-tag (e.g. 1.6.2)
- create a Pre-Release based on the tagged sources  
This steps mainly verifies everything is working as expected for the final release step.  Release-Perform as the next step will checkout the same sources based on the created tag to build and upload the final release.  
Use the profile: `release` in order to sign the code and upload it to Nexus  
No need to run the tests, as they have been automatically executed already on Travis  
`mvn -Darguments=-DskipTests release:prepare -P release`  
- Accept or change the proposed version numbers.  
- The SCM-Release-Tag label should be the version number. E.g. `1.6.2`  
Use the built Pre-Release from folder:  
`apimanager-swagger-promote\modules\distribution\target`  
to perform some final tests.  
If you are not happy with the actual build for any reason, you need to rollback the actual release prepare:
`mvn release:rollback`  
Quite often the created tag isn't removed automatically. You need to delete it to re-execute release:prepare:  
`git tag -d 1.6.2`  
`git push origin :refs/tags/1.6.2`  
7. Finally release it  
`mvn -Darguments=-DskipTests release:perform -P release`  
This automatically uploads the release to Maven-Central  
8. Upload the Distribution package to the release on Github  
9. Create the Chocolatey package  
`copy target\checkout\modules\distribution\target\apimanager-swagger-promote-1.6.2.zip tools\choco\axway-swagger-promote\tools`  
`cd tools\choco\axway-swagger-promote`  
Adjust the version number in file: `axway-swagger-promote.nuspec`  
`choco install checksum`  
`checksum sha256 -f tools\apimanager-swagger-promote-1.6.2.zip`  
Copy the generated checksum and insert it into the file: `tools\chocolateyinstall.ps1`  
`choco pack`  
Test the installation of this package:  
`choco install axway-swagger-promote -s .`  
Execute the Swagger-Promote as shown:  
`api-import` or `api-export`  
Finally push the package to Chocolatey:  
`choco push --api-key <api-key-goes-here> axway-swagger-promote.1.6.2.nupkg`
