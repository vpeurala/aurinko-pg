#!/usr/bin/env bash
./docker/build.sh
./docker/run.sh --force
gradle build
gradle assemble
cp build/libs/aurinko-pg-0.4.jar $HOME/Projects/Wunderdog/Arctech/arctech4/lib/aurinko-pg-0.4.jar
docker rm -f jaanmurtaja-db

