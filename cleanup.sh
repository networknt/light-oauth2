#!/bin/bash

# The following commands remove all oauth2 containers and images in order to start a brand new build.
# It should be called if you have changed any code in one of services.

docker ps -a | awk '{ print }' | grep lightoauth2_oauth2 | awk '{print $1}' | xargs -I {} docker rm {}
docker images | awk '{ print }' | grep lightoauth2_oauth2 | awk '{print $3}' | xargs -I {} docker rmi {}
