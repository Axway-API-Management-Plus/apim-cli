
# Purpose
The project is using Integration-Tests to simulate tasks the API-Developer is doing. That means, creating the API for the first time, doing frequent changes, subscribe with applications, etc.
For that TravisCI is using, which is starting an API-Manager V7.x Docker-Container to perform these integration tests. This document describes the steps to setup Github to run Integration tests.

## Steps
- Use a license without hostname binding, The license file is used as environment variable on github CI / CD pipeline.  To use license file as environment variable, do base64 encodeing
    ```bash
    base64 -i ~/Downloads/API-7.7-Docker-Temp.lic
    
    ```
    - Go to Actions secrets and variables on Github page and update variable APIM_LIC
- Create a service account on Amplify and copy client_id and password, the credentials is used to download the docker images from Axway repository. 
  
  - Go to Actions secrets and variables on Github page and update variable
    Perform the following steps:
    - Update Github variable AXWAY_DOCKER_REG_PASS with client_id
    - Update Github variable AXWAY_DOCKER_REG_USER with client_secret


