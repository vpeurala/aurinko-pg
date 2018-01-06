#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd .;
cd ${PROJECT_ROOT};
docker exec --interactive --tty --user=jaanmurtaja jaanmurtaja-db /bin/bash;
popd;
