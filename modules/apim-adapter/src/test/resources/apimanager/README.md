# Not used - Migrated to official docker image
# Purpose
The project is using Integration-Tests to simulate tasks the API-Developer is doing. That means, creating the API for the first time, doing frequent changes, subscribe with applications, etc. 
For that Github Action is using, which is starting an API-Manager V7.x Docker-Container to perform these integration tests. This document describes the steps needed to build the Docker-Image.

## Steps
Integration test Github action uses APIM V7 docker image to run the integration tests. 
- Update latest docker image with tag
- Update supported Cassandra version 
- Update the docker cahes

Github workflow file integration-test.yaml 

```yaml
  name: APIM CLI Integration Tests

  on: [push]

  env:
      CASSANDRA_DOCKER_IMAGE: cassandra:4.0.13
      APIM_DOCKER_IMAGE: docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240530-2-BN0004-ubi9
      CACHE_FILE_APIM: api-manager_7_7_20240530.cache.tar
      CACHE_FILE_CASSANDRA: cassandra_4_0_13.cache.tar
      FED_FILE: swagger-promote-7.7-20240530.fed
      LOG_LEVEL: info
```


## Update fed file to newer version. 

### Upgrade fed file

- Run upgradeconfig CLI command to update fed file to newer version

```bash
docker run -it  -v /home/axway/apim-cli/modules/apim-adapter/src/test/resources/apimanager:/opt/Axway/apiprojects docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240530-1-BN0092-ubi9 /bin/sh /opt/Axway/apigateway/posix/bin/upgradeconfig -f /opt/Axway/apiprojects/swagger-promote-7.7-20240228.fed -o /opt/Axway/apiprojects/new
```

Above command creates new folder named **new** under modules/apim-adapter/src/test/resources/apimanager and files (suffix with .fed, .pol, .env)

- Rename fed folder
```bash
mv e4b134f7-905b-4c56-ab40-221db6c931c9.fed swagger-promote-7.7-20240530.fed
```

- Update fed file name in workflow file integration-test.yaml 
```yaml
env:
      FED_FILE: swagger-promote-7.7-20240530.fed
```

## Update APIM license 

- Create base64 encoded version of license.
```bash
base64 -i ~/Downloads/API-7.7-Docker-Temp.lic
```
- Copy the base64 content and update Github Secrets and Variables

Secret name - APIM_LIC

## Push all changes to git, it will trigger the integration test. 
