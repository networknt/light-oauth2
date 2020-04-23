#!/bin/bash
echo "Build the view in in test mode"
yarn build
echo "Build completed in build folder, start copying to remote test2 server"
scp -r ./build steve@test2:/home/steve/light-chain/light-config-test/light-router/test-direct/signin
echo "Copied!"
