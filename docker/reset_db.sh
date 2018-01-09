#!/usr/bin/env bash
PGPASSWORD=argxBX4DxWJKC7st psql -h "0.0.0.0" -p 5432 -U jaanmurtaja jaanmurtaja < drop.sql
PGPASSWORD=argxBX4DxWJKC7st psql -h "0.0.0.0" -p 5432 -U jaanmurtaja jaanmurtaja < init.sql

