#!/bin/bash
echo "Build the view in in test mode"
yarn build
echo "Build completed in build folder, start copying to remote test2 server"
scp -r ./build/* steve@test2:/home/steve/networknt/light-config-test/light-router/test-portal/signin/build
echo "Copied!"
