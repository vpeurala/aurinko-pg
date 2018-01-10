#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd . >/dev/null;
cd ${PROJECT_ROOT}/docker;
./build.sh && ./run.sh --force;
popd >/dev/null;

