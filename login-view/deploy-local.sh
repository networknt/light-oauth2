#!/bin/bash
echo "Build the view in in test mode"
yarn build
echo "Build completed in build folder, start copying to local folder"
rm -rf /home/steve/light-chain/light-config-test/light-router/local-direct/signin/build
cp -r ./build /home/steve/light-chain/light-config-test/light-router/local-direct/signin
echo "Copied!"
