Submodule to create a new Release-Package

Instructions:
1. Merge everything from develop into master branch
2. Open the main project directory
cd C:\workspaces\api-management\apimanager-swagger-promote
3. Validate a clean state on master
git branch -avv
2. From master create a new release branch
git checkout -b release/1.5.4 origin/master
3. Pump the version number
4. Create a package to test with
mvn clean package -DskipTests
5. Perform local test
6. Checkin on release branch
git push origin release/0.1.0
6. Prepare the release
mvn -Darguments=-DskipTests release:prepare
7. Release it
mvn -Darguments=-DskipTests release:perform