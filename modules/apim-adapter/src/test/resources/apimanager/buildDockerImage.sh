#!/bin/sh

currentDir=$PWD

trap exitScript INT

function exitScript() {
	rc=$1
	cd $currentDir
	if [[ $rc = 10 ]]; then
		echo "Supported versions"
		echo "`basename $0` 7.7-20210530"
		echo "`basename $0` 7.7-20210330"
		echo "`basename $0` 7.7-20200930"
		echo "`basename $0` 7.7-20200730"
		echo "`basename $0` 7.7-20200530"
		echo "`basename $0` 7.7-20200331"
		echo "`basename $0` 7.7-20200130"
		echo "`basename $0` 7.7-SP2"
		echo "`basename $0` 7.6.2-SP5"
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
testSources="$HOME/apim-cli/modules/apim-adapter/src/test/resources/apimanager"
buildDir="$HOME/apim-cli-dockerimage"

echo "Creating docker image for version $version"

case "$version" in
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
	7.7-20200730)
		fedFile="swagger-promote-7.7-20200130.fed"
		installer="APIGateway_7.7.20200730_Install_linux-x86-64_BN02.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	7.7-20200530)
		fedFile="swagger-promote-7.7-20200130.fed"
		installer="APIGateway_7.7.20200530_Install_linux-x86-64_BN02.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	7.7-20200331)
		fedFile="swagger-promote-7.7-20200130.fed"
		installer="APIGateway_7.7_Install_linux-x86-64_BN3.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	7.7-20200130)
		fedFile="swagger-promote-7.7-20200130.fed"
		installer="APIGateway_7.7.20200130_Install_linux-x86-64_BN02.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	7.7-SP2)
		fedFile="swagger-promote-7.7.fed"
		installer="APIGateway_7.7_SP2_linux-x86-64_BN201912201.run"
		dockerScripts="APIGateway_7.7.20200130-1_DockerScripts.tar.gz";;
	7.6.2-SP5)
		fedFile="swagger-promote-7.6.2.fed"
		installer="APIGateway_7.6.2_SP5_Install_linux-x86-64_BN20200717.run"
		dockerScripts="APIGateway_7.6.2-8_ScriptsPackageDocker_linux-x86-64_BN27072018.tar.gz";;
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


cd *emt*

echo "Using reduced runInstall.sh"
cp -v $testSources/runInstall.sh Dockerfiles/gateway-base/scripts/runInstall.sh

./gen_domain_cert.py --default-cert --force
echo "########### Create Base-Image        ###################"
./build_base_image.py --installer=../$installer --os=centos7
echo "########### Create API-Gateway-Image ###################"
./build_gw_image.py --license=../multiple.lic --default-cert --fed=$testSources/$fedFile --merge-dir $testSources/merge-dir/apigateway --out-image=api-gw-mgr:$version

docker tag api-gw-mgr:$version docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:$version
echo "########### Push Image               ###################"
docker push docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:$version
