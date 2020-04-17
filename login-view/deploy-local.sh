#!/bin/bash
echo "Build the view in in test mode"
yarn build
echo "Build completed in build folder, start copying to local folder"
rm -rf /home/steve/networknt/light-config-test/light-router/local-portal/signin/build
cp -r ./build /home/steve/networknt/light-config-test/light-router/local-portal/signin
echo "Copied!"
