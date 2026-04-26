#!/bin/bash

mvn package
mkdir -p binaries
cp target/md2gem-0.1-jar-with-dependencies.jar binaries/md2gem-0.1.jar

