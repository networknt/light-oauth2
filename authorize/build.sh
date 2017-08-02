#!/bin/bash

set -ex

CURRENT_DIR="${PWD##*/}"
IMAGE_NAME="networknt/oauth-$CURRENT_DIR"
TAG="${1}"

if docker inspect ${IMAGE_NAME} &> /dev/null; then
	docker rmi -f ${IMAGE_NAME}:latest
	docker rmi -f ${IMAGE_NAME}:${TAG}
fi

docker build -t ${IMAGE_NAME} .
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:latest
docker tag ${IMAGE_NAME} ${IMAGE_NAME}:${TAG}
docker push ${IMAGE_NAME}
