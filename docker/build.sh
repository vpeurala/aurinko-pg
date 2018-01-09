#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd . >/dev/null;
cd ${PROJECT_ROOT}/docker;
docker build --file Dockerfile --tag vpeurala/aurinko-pg-9.5.5:latest .;
popd >/dev/null;
