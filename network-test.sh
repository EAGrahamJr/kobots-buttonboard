#!/bin/sh

# check for a host or Google DNS server
host="${1:-8.8.8.8}"

pingcheck() {
  ping -n -c 1 -w 5 $1 >/dev/null 2>&1
}

# Do you want a timeout ?
while :; do
  pingcheck ${host} && exit 0
  sleep 10
done
