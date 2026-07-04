#!/usr/bin/env bash
# Query monitor for primary and kill its pid file
# Send GET_PRIMARY to monitor
resp=$(echo "GET" | nc localhost 9000 -w 1 2>/dev/null)
# Our monitor expects a GET_PRIMARY message in JSON format; instead use a simple socket exchange:
# We'll implement a tiny Java helper call to ask monitor; but to keep script simple, we check logs.
primary=$(grep "Elected new primary" logs/monitor.log | tail -n1 | awk -F': ' '{print $3}' | tr -d ' \r\n')
if [ -z "$primary" ]; then
  echo "Primary not found in logs. Inspect logs/monitor.log"
  exit 1
fi
echo "Primary id from logs: $primary"
pidfile="pids/server-${primary}.pid"
if [ -f "$pidfile" ]; then
  pid=$(cat "$pidfile")
  echo "Killing primary pid $pid"
  kill -9 $pid || true
  rm -f "$pidfile"
else
  echo "PID file $pidfile not found. You may need to kill process manually."
fi