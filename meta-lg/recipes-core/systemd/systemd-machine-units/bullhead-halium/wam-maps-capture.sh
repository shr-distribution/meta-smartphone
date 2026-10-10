#!/bin/sh
# Bring-up diagnostic for the Nexus 5X, see wam-maps-capture.service: copy the memory map of
# each new WebAppMgr browser process to /var/log/wam-maps/ while it is still alive.
d=/var/log/wam-maps
mkdir -p "$d"
rm -f "$d"/*
seen=" "
i=0
n=0
# 6000 x 0.1 s: the first ten minutes of the boot, and at most 20 processes.
while [ "$i" -lt 6000 ] && [ "$n" -lt 20 ]; do
    for p in $(pidof WebAppMgr); do
        case "$seen" in *" $p "*) continue ;; esac
        seen="$seen$p "
        # Only the browser process; the renderers and zygotes run with --type=.
        tr '\0' ' ' < "/proc/$p/cmdline" 2>/dev/null | grep -q -- '--type=' && continue
        n=$((n + 1))
        # Its GPU thread starts a little later; the crash came about a second after start.
        sleep 0.3
        cat "/proc/$p/maps" > "$d/maps-$p.txt" 2>/dev/null
        cat "/proc/$p/status" > "$d/status-$p.txt" 2>/dev/null
    done
    sleep 0.1
    i=$((i + 1))
done
