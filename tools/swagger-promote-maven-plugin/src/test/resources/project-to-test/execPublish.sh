#!/bin/bash

HOST=localhost
USER=apiadmin
PASS=changeme
API=./src/main/resources/apis/adminui-v1-min.json
CONFIG=./src/main/resources/config/adminui-v1-min-config.json

## mvn clean test -Daxway.host=$HOST -Daxway.port=443 -Daxway.username=$USER -Daxway.password=$PASS -Daxway.apiDefinition=$API -Daxway.contract=$CONFIG


API=./src/main/resources/apis/adminui-v2-min.json
CONFIG=./src/main/resources/config/adminui-v2-min-config.json

mvn clean test -Daxway.host=$HOST -Daxway.port=443 -Daxway.username=$USER -Daxway.password=$PASS -Daxway.apiDefinition=$API -Daxway.contract=$CONFIG -P TEST_ENV