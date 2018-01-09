#!/usr/bin/env bash
docker images --filter="reference=vpeurala/aurinko-pg-9.5.5:latest" --format="{{.Repository}}:{{.Tag}}" --no-trunc;
