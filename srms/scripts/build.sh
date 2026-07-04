#!/usr/bin/env bash
# Create a file that servers check to add extra delay to heartbeat loop
touch /tmp/delay_heartbeats
echo "Delay enabled for 10s"
sleep 10
rm -f /tmp/delay_heartbeats
echo "Delay removed"