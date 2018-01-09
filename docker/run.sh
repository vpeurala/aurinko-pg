#!/usr/bin/env bash
PROJECT_ROOT=$(git rev-parse --show-toplevel);
pushd . >/dev/null;
cd ${PROJECT_ROOT}/docker;
CONTAINER_STATUS=$(./checks/check_container_status.sh);
echo "Container status: $CONTAINER_STATUS";
if [[ "$CONTAINER_STATUS" == "jaanmurtaja-db: Up"* ]]; then
  echo "The container is already running.";
  if [[ "$1" == "-f" || "$1" == "--force" ]]; then
    echo "You used the \"force\" option \"$1\", so we destroy the current container and start a new one.";
    docker rm -f jaanmurtaja-db;
    echo "Old container destroyed, starting a new one...";
  else
    echo "Run with the \"force\" (-f, --force) option if you want to destroy the current container and start a new one.";
    echo "Like this: \"$0 --force\".";
    echo "Exiting now and leaving the existing container running.";
    exit 1;
  fi
fi
docker run --detach --hostname jaanmurtaja-db --name jaanmurtaja-db --publish 5432:5432 --user jaanmurtaja vpeurala/aurinko-pg-9.5.5:latest;
popd >/dev/null;
