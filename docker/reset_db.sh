#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd . >/dev/null;
cd ${PROJECT_ROOT}/docker;
PGPASSWORD=argxBX4DxWJKC7st psql -h "0.0.0.0" -p 5432 -U jaanmurtaja jaanmurtaja < drop.sql
PGPASSWORD=argxBX4DxWJKC7st psql -h "0.0.0.0" -p 5432 -U jaanmurtaja jaanmurtaja < init.sql
popd >/dev/null;

