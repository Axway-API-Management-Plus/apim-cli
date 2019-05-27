#!/bin/sh

if [ -f $CACHE_FILE_TO_USE ]
then
	echo "Loading docker image: $DOCKER_IMAGE_TO_USE from $CACHE_FILE_TO_USE"
	gunzip -c $CACHE_FILE_TO_USE | docker load
else 
	echo "Pulling APIM docker from registry, this will take a while"
	docker pull $DOCKER_IMAGE_TO_USE
fi


