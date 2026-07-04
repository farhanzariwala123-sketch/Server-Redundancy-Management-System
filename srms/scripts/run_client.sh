#!/usr/bin/env bash
if [ -z "$1" ]; then
  echo "Usage: run_client.sh <payload>"
  exit 1
fi
java -cp bin client.ClientMain "$1"