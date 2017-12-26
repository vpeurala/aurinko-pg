#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd .;
cd ${PROJECT_ROOT};
docker run --detach --hostname jaanmurtaja-db --name jaanmurtaja-db --publish 5432:5432 --user jaanmurtaja aurinko/postgresql-9.5.5:latest
popd;

