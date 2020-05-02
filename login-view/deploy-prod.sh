#!/bin/bash
echo "Build the view in prod mode"
yarn build
echo "Build completed in build folder."
echo "start copying to remote prod1 server."
ssh prod1 "rm -rf ~/networknt/light-config-prod/prod1/signin/build"
scp -r ./build prod1:/home/steve/networknt/light-config-prod/prod1/signin
echo "start copying to remote prod2 server."
ssh prod2 "rm -rf ~/networknt/light-config-prod/prod2/signin/build"
scp -r ./build prod2:/home/steve/networknt/light-config-prod/prod2/signin
echo "Copied!"
