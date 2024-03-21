#!/bin/sh

currentDir=$PWD

trap exitScript INT

function exitScript() {
	rc=$1
	cd $currentDir
	if [[ $rc = 10 ]]; then
		echo "Supported versions"
		echo "`basename $0` 7.7-20231130"
		echo "`basename $0` 7.7-20230830"
		echo "`basename $0` 7.7-20230530"
		echo "`basename $0` 7.7-20230228"
		echo "`basename $0` 7.7-20221130"
		echo "`basename $0` 7.7-20220830"
		echo "`basename $0` 7.7-20220530"
		echo "`basename $0` 7.7-20220228"
		echo "`basename $0` 7.7-20211130"
		echo "`basename $0` 7.7-20210830"
		echo "`basename $0` 7.7-20210530"
		echo "`basename $0` 7.7-20210330"
		echo "`basename $0` 7.7-20200930"
	fi
	exit $rc
}

if [ $# -eq 0 ]
then
	echo "Missing an API-Gateway version"
	exitScript 10
fi

version=$1
dockerScripts="APIGateway_7.7.20210330-DockerScripts-2.2.0-2.tar.gz"
testSources="$HOME/apimcli/apim-cli/modules/apim-adapter/src/test/resources/apimanager"
buildDir="$HOME/apim-cli-dockerimage"

echo "Creating docker image for version $version"

case "$version" in
   7.7-20231130)
           fedFile="swagger-promote-7.7-20231130.fed"
           installer="APIGateway_7.7.20231130_Install_linux-x86-64_BN02.run"
           dockerScripts="APIGateway_7.7.20231130-DockerScripts-2.13.0.tar.gz"
           dockerScriptsDir="apigw-emt-scripts-2.13.0";;
    7.7-20230830)
        fedFile="swagger-promote-7.7-20230830.fed"
        installer="APIGateway_7.7.20230830_Install_linux-x86-64_BN03.run"
        dockerScripts="APIGateway_7.7.20230830-DockerScripts-2.12.0.tar.gz"
        dockerScriptsDir="apigw-emt-scripts-2.12.0";;
    7.7-20230530)
        fedFile="swagger-promote-7.7-20230530.fed"
        installer="APIGateway_7.7.20230530_Install_linux-x86-64_BN02.run"
        dockerScripts="APIGateway_7.7.20230530-DockerScripts-2.10.0.tar.gz"
        dockerScriptsDir="apigw-emt-scripts-2.10.0";;
    7.7-20230228)
        fedFile="swagger-promote-7.7-20230228.fed"
        installer="APIGateway_7.7.20230228_Install_linux-x86-64_BN01.run"
        dockerScripts="APIGateway_7.7.20230228-DockerScripts-2.8.0.tar.gz"
        dockerScriptsDir="apigw-emt-scripts-2.8.0";;
	7.7-20221130)
		fedFile="swagger-promote-7.7-20221130.fed"
		installer="APIGateway_7.7.20221130_Install_linux-x86-64_BN03.run"
		dockerScripts="APIGateway_7.7.20221130-DockerScripts-2.7.0.tar.gz"
		dockerScriptsDir="apigw-emt-scripts-2.7.0";;
	7.7-20220830)
		fedFile="swagger-promote-7.7-20220830.fed"
		installer="APIGateway_7.7.20220830_Install_linux-x86-64_BN04.run"
		dockerScripts="APIGateway_7.7.20220830-DockerScripts-2.6.0-1.tar.gz"
		dockerScriptsDir="apigw-emt-scripts-2.6.0";;
	7.7-20220530)
		fedFile="swagger-promote-7.7-20220530.fed"
		installer="APIGateway_7.7.20220530_Install_linux-x86-64_BN02.run"
		dockerScripts="APIGateway_7.7.20220530-DockerScripts-2.5.0-1.tar.gz"
		dockerScriptsDir="apigw-emt-scripts-2.5.0";;
	7.7-20220228)
		fedFile="swagger-promote-7.7-20220228.fed"
		installer="apigw-installer-7.7.0.20220228-1-linux64.run"
		dockerScripts="apigw-emt-scripts-2.4.0-20220222.150412-10.tar.gz"
		dockerScriptsDir="apigw-emt-scripts-2.4.0-SNAPSHOT";;
	7.7-20211130)
		fedFile="swagger-promote-7.7-20211130.fed"
		installer="apigw-installer-7.7.0.20211130-1-linux64.run";;
	7.7-20210830)
		fedFile="swagger-promote-7.7-20210830.fed"
		installer="APIGateway_7.7.20210830_Install_linux-x86-64_BN02.run";;
	7.7-20210530)
		fedFile="swagger-promote-7.7-20210530.fed"
		installer="APIGateway_7.7.20210530_Install_linux-x86-64_BN02.run";;
	7.7-20210330)
		fedFile="swagger-promote-7.7-20210330.fed"
		installer="APIGateway_7.7.20210330_Install_linux-x86-64_BN06.run";;
	7.7-20200930)
		fedFile="swagger-promote-7.7-20200930.fed"
		installer="APIGateway_7.7.20200930_Install_linux-x86-64_BN03.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	*)
		echo "Unknown version $version"
		exitScript 10;;
esac
cd $buildDir

echo "Creating image based on installer: $installer and FED-File: $fedFile"
if [[ ! -f "$buildDir/$installer" ]]; then
	echo "Installer not found: $buildDir/$installer"
	exitScript 1
fi

if [[ ! -f "$buildDir/multiple.lic" ]]; then
	echo "License-File not found: $buildDir/multiple.lic"
	exitScript 1
fi

if [[ ! -f "$testSources/$fedFile" ]]; then
	echo "FED-File not found: $testSources/$fedFile"
	exitScript 1
fi

echo "Installer and FED-File found ..."

echo "Trying to login into Docker-Registry"
docker login docker-registry.demo.axway.com

if tar xfz $dockerScripts; then
	echo "Docker scripts extracted"
else
	echo "Error extracting Docker-Scripts: $dockerScripts"
	exit 99
fi

if [[ ! -z "$dockerScriptsDir" ]]; then
	cd "$dockerScriptsDir"
else
	cd *emt*
fi

echo "Using reduced runInstall.sh"
cp -v $testSources/runInstall.sh Dockerfiles/gateway-base/scripts/runInstall.sh

./gen_domain_cert.py --default-cert --force
echo "########### Create Base-Image        ###################"
./build_base_image.py --installer=../$installer --os=centos7 --out-image apigw-base:$version
echo "########### Create API-Gateway-Image ###################"
./build_gw_image.py --license=../multiple.lic --default-cert --fed=$testSources/$fedFile --merge-dir $testSources/merge/apigateway --parent-image apigw-base:$version --out-image=docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:$version

echo "########### Push Image               ###################"
docker push docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:$version
