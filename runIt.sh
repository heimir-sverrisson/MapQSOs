#!/bin/bash
set -x
cd bin; cp com/coolprimes/mapqsos/* ../runtime/com/coolprimes/mapqsos
cd ../runtime
java -cp slf4j-api-1.7.25.jar:slf4j-simple-1.7.25.jar:. \
    com.coolprimes.mapqsos.Runner localhost 2237
