#!/bin/bash
echo "Build the view in in test mode"
yarn build
echo "Build completed in build folder."
echo "start copying to remote test1 server."
ssh test1 "rm -rf ~/light-chain/light-config-test/test1/signin/build"
scp -r ./build test1:/home/steve/light-chain/light-config-test/test1/signin
echo "start copying to remote test2 server."
ssh test2 "rm -rf ~/light-chain/light-config-test/test2/signin/build"
scp -r ./build test2:/home/steve/light-chain/light-config-test/test2/signin
echo "Copied!"
