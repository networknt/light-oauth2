#!/bin/bash

set -ex

CURRENT_DIR="${PWD##*/}"
IMAGE_NAME="networknt/oauth2-$CURRENT_DIR"
TAG="${1}"

if [docker images | grep -c ${IMAGE_NAME} -ge 0]; then
	docker rmi -f ${IMAGE_NAME}
fi

docker build -t ${IMAGE_NAME} .
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:latest
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:${TAG}
docker push ${IMAGE_NAME}