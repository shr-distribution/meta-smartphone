#!/bin/sh

echo "Waiting for container to come up ..."
lxc-wait -n android -s RUNNING

while true; do
        echo "Checking for boot done flag ..."
        sleep 1
        [ "$(getprop sys.init_boot_completed)" = "1" ] && break
done

sleep 1


