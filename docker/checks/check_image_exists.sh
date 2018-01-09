#!/usr/bin/env bash
docker images --filter="reference=aurinko/postgresql-9.5.5:latest" --format="{{.Repository}}:{{.Tag}}" --no-trunc;
