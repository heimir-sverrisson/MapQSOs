#!/bin/bash
# set -x
#
pushd build/classes/java/main
cp -R com ../../../../runtime
popd
cd runtime
java -cp slf4j-api-1.7.25.jar:slf4j-simple-1.7.25.jar:. \
    com.coolprimes.mapqsos.Runner localhost 2237
