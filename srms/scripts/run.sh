#!/usr/bin/env bash
set -e
mkdir -p pids logs
bash scripts/build.sh

# Start Monitor
nohup java -cp bin monitor.MonitorMain 9000 > logs/monitor.out 2>&1 &
echo $! > pids/monitor.pid
sleep 1

# Start 3 servers with ids 1,2,3 and ports 10001,10002,10003
nohup java -cp bin server.ServerMain 1 10001 localhost 9000 > logs/server1.out 2>&1 &
echo $! > pids/server-1.pid
nohup java -cp bin server.ServerMain 2 10002 localhost 9000 > logs/server2.out 2>&1 &
echo $! > pids/server-2.pid
nohup java -cp bin server.ServerMain 3 10003 localhost 9000 > logs/server3.out 2>&1 &
echo $! > pids/server-3.pid

echo "Started monitor and 3 servers"