#!/bin/sh

if [ -f $CACHE_FILE_APIM_7_7 ]
then
	echo "Loading docker image: $APIM_DOCKER_IMAGE from $CACHE_FILE_APIM_7_7"
	gunzip -c $CACHE_FILE_APIM_7_7 | docker load
else 
	echo "Pulling APIM docker from registry, this will take a while"
	docker pull $APIM_DOCKER_IMAGE_7_7
fi


