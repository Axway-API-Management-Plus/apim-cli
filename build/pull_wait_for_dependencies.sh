#!/bin/sh

if [ -f $CACHE_FILE_WAIT_FOR_DEPENDENCIES ]
then
	echo "Loading docker image: dadarek/wait-for-dependencies from $CACHE_FILE_WAIT_FOR_DEPENDENCIES"
	gunzip -c $CACHE_FILE_WAIT_FOR_DEPENDENCIES | docker load
else 
	echo "Pulling dadarek/wait-for-dependencies docker from Docker-Hub"
	docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS
	docker pull dadarek/wait-for-dependencies
fi


