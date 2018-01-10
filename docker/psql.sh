#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd . >/dev/null;
cd ${PROJECT_ROOT}/docker;
docker exec --interactive --tty --user=jaanmurtaja jaanmurtaja-db psql;
popd >/dev/null;
