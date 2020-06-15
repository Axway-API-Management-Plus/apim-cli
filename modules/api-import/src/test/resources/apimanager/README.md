
# Purpose
The project is using Integration-Tests to simulate tasks the API-Developer is doing. That means, creating the API for the first time, doing frequent changes, subscribe with applications, etc. 
For that TravisCI is using, which is starting an API-Manager V7.x Docker-Container to perform these integration tests. This document describes the steps needed to build the Docker-Image.

## Steps
Download the API-Gateway/API-Manager release from support.axway.com you want to test with and have the following ready:
- APIGateway_x.x.x_xxx_Install_linux-x86-64_BNxxxxxx.run
- APIGateway_x.x.x-x_ScriptsPackageDocker_linux-x86-64_BNxxxxxxx.tar.gz
- a license without hostname binding
Copy everything to a system having docker installed. 
Perform the following steps:
```
    cd $HOME
    git clone https://github.com/Axway-API-Management-Plus/apim-cli.git
    mkdir apim-cli-dockerimage
    cp APIGateway_x.x.x_xxx_Install_linux-x86-64_BNxxxxxx.run apim-cli-dockerimage
    cp apigw-emt-scripts-2.0.0-20190409.161128-24.tar.gz apim-cli-dockerimage
    cd apim-cli-dockerimage
    tar xvfz apigw-emt-scripts-2.0.0-20190409.161128-24.tar.gz
    cd apigw-emt-scripts-2.0.0-SNAPSHOT
    ./build_base_image.py --installer=../APIGateway_x.x.x_xxx_Install_linux-x86-64_BNxxxxxx.run --os=centos7
    ./gen_domain_cert.py --default-cert
    ./build_gw_image.py --license=multiple.lic --default-cert --fed=$HOME/apimanager-swagger-promote/modules/swagger-promote-core/src/test/resources/apimanager/swagger-promote-7.6.2.fed --merge-dir $HOME/apimanager-swagger-promote/modules/swagger-promote-core/src/test/resources/apimanager/merge-dir/apigateway --out-image=api-gw-mgr:7.6.2-SP3
    docker images
    docker tag api-gw-mgr:7.6.2-SP3 docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:7.6.2-SP3
    docker login docker-registry.demo.axway.com
    docker push docker-registry.demo.axway.com/swagger-promote/api-mgr-with-policies:7.6.2-SP3
```

### Added Untrusted Docker-Registry
```
vi /etc/docker/daemon.json
```
Add the following
```json
{
  "insecure-registries" : ["docker-registry.demo.axway.com"]
}
systemctl restart dockerd
```

Please note, that you need Write-Permissions to the Docker-Repository to push the image!

To enable the image, please adjust the image referred in the Travis-CI configuration file:
```
edit .travis.yml
and change:
Add/register your new Docker-Image and reference it in the following environment variables:
- DOCKER_IMAGE_TO_USE=$APIM_DOCKER_IMAGE_7_6_2
- CACHE_FILE_TO_USE=$CACHE_FILE_APIM_7_6_2
```
After checkin & commit a Travis-CI build is started using the provided Docker-Image.
