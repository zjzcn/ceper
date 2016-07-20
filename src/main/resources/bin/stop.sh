#!/bin/sh

PIDS=$(ps ax | grep -i 'ceper\.Engine' | grep java | grep -v grep | awk '{print $1}')

if [ -z "$PIDS" ]; then
  echo "No process to stop"
  exit 1
else 
  kill -s TERM $PIDS
fi