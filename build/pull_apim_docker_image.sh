#!/bin/sh

if [ -f $CACHE_FILE_APIM ]
then
	echo "Loading API-Manager docker image: $APIM_DOCKER_IMAGE from $CACHE_FILE_APIM"
	gunzip -c $CACHE_FILE_APIM | docker load
else
	echo "Pulling APIM docker from registry, this will take a while"
	docker pull $APIM_DOCKER_IMAGE
fi
