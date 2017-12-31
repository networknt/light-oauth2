#!/bin/bash

#
# Purpose: Publish docker image new tag to docker hub.
#
# Author:  Dinesh Alapati, dine.alapati@gmail.com
#
# Parameters:
#   $1:  version
#

set -ex

VERSION=$1
IMAGE_NAME="networknt/oauth2-client"

showHelp() {
    echo " "
    echo "Error: $1"
    echo " "
    echo "    build.sh [VERSION]"
    echo " "
    echo "    where [VERSION] version of the docker image that you want to publish (example: 0.0.1)"
    echo " "
    echo "    example 1: ./build.sh 0.0.1"
    echo " "
}

build() {
    echo "Building ..."
    mvn clean install
    echo "Successfully built!"
}

cleanup() {
    if [[ "$(docker images -q $IMAGE_NAME 2> /dev/null)" != "" ]]; then
        echo "Removing old $IMAGE_NAME images"
        docker images | grep $IMAGE_NAME | awk '{print $3}' | xargs docker rmi -f
        echo "Cleanup completed!"
    fi
}

publish() {
    echo "Building Docker image with version $VERSION"
    docker build -t $IMAGE_NAME:$VERSION -t $IMAGE_NAME:latest -f ./docker/Dockerfile . --no-cache=true
    docker build -t $IMAGE_NAME:$VERSION-redhat -f ./docker/Dockerfile-Redhat . --no-cache=true
    echo "Images built with version $VERSION"
    echo "Pushing image to DockerHub"
    docker push $IMAGE_NAME
    echo "Image successfully published!"
}

if [ -z $VERSION ]; then
    showHelp "[VERSION] parameter is missing"
    exit
fi

build;
cleanup;
publish;
