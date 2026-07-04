#!/usr/bin/env bash
set -e
for f in pids/*.pid; do
  if [ -f "$f" ]; then
    pid=$(cat "$f")
    echo "Killing $pid"
    kill $pid || true
    rm -f "$f"
  fi
done