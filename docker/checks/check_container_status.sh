#!/usr/bin/env bash
docker ps --all --filter="name=jaanmurtaja-db" --format="{{.Names}}: {{.Status}}" --no-trunc;

