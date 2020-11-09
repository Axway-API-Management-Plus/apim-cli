#!/bin/sh

echo "CACHE_FILE_APIM: $CACHE_FILE_APIM"
echo "SKIP_CACHE: $SKIP_CACHE"

echo "Listing $CACHE_DIR"
ls -l $CACHE_DIR

if [ -f $CACHE_FILE_APIM -a "$SKIP_CACHE" != "true" ]
then
	echo "Using cached API-Manager docker image: $APIM_DOCKER_IMAGE from $CACHE_FILE_APIM"
	gunzip -c $CACHE_FILE_APIM | docker load
else
	echo "Pulling APIM docker from registry, this will take a while"
	docker login --username $AXWAY_DOCKER_REG_USER --password $AXWAY_DOCKER_REG_PASS docker-registry.demo.axway.com
	docker pull $APIM_DOCKER_IMAGE
fi
