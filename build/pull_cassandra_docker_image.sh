#!/bin/sh

if [ -f $CACHE_FILE_CASSANDRA ]
then
	echo "Loading docker image: $CASSANDRA_DOCKER_IMAGE from $CACHE_FILE_CASSANDRA"
	gunzip -c $CACHE_FILE_CASSANDRA | docker load
else 
	echo "Pulling CASSANDRA docker from registry, this will take a while"
	docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASS
	docker pull $CASSANDRA_DOCKER_IMAGE
fi


