#!/bin/sh
# Trace harness for radon bring-up: run the real init under xtrace and stream
# the trace to an unused flash partition, so it can be read back with mtkclient
# after the device hangs or resets.
#
# WHY THIS EXISTS
#
# On 23 Sep 2026 this device hung in the initramfs with no diagnostic channel
# at all: lk sets console=ttyS0,921600n1 (no UART is exposed on a retail FLX1s),
# the panel stayed black, the USB gadget never enumerated - not even in the
# enable_adb image, which panics to adbd *before* mountroot - and expdb came
# back with off_linux:0x0, i.e. lk found no kernel log to copy. Every test was
# therefore one bit of information: "hangs" or "does not hang".
#
# This turns each boot into a full execution trace instead.
#
# WHERE IT WRITES
#
# init_boot_b. The MT6877 scatter declares it 8 MiB with
# "is_download: false, file_name: NONE", and this is a header v2 device, so no
# init_boot is part of the boot path at all (same as mp01). FuriLabs populate
# neither slot. Nothing on this device reads it, which is exactly what a scratch
# area needs to be.
#
# Read it back with:
#   ( cd ~/mtkclient && python3 mtk.py r init_boot_b trace.bin )
#   strings trace.bin | sed -n '/LUNEOS-TRACE-BEGIN/,$p' | less
#
# HOW IT SURVIVES mountroot
#
# halium's mountroot does "exec &>/dev/kmsg", which would redirect a normal
# "set -x" trace into a kmsg we cannot read. /bin/sh here is bash (the initramfs
# image installs it and it takes over /bin/sh), so BASH_XTRACEFD is available:
# the trace goes to fd 9, which that exec does not touch.
#
# The real init is SOURCED rather than exec'd, so this shell - PID 1 - keeps
# xtrace enabled for the whole run.

TRACE=/trace.log
SCRATCH_PARTNAME=${SCRATCH_PARTNAME:-init_boot_b}
MAXBYTES=6291456           # keep under the 8 MiB partition

echo "LUNEOS-TRACE-BEGIN $(date 2>/dev/null)" > "$TRACE"

# Background flusher: find the scratch partition once /sys is up, then write the
# trace out repeatedly. Whatever was last flushed survives the reset, so the
# final lines show where the boot died.
(
    part=""
    while : ; do
        if [ -z "$part" ] && [ -d /sys/class/block ]; then
            for blk in /sys/class/block/*; do
                [ -f "$blk/uevent" ] || continue
                pn=$(sed -n 's/^PARTNAME=//p' "$blk/uevent" 2>/dev/null)
                [ "$pn" = "$SCRATCH_PARTNAME" ] || continue
                maj=$(sed -n 's/^MAJOR=//p' "$blk/uevent" 2>/dev/null)
                min=$(sed -n 's/^MINOR=//p' "$blk/uevent" 2>/dev/null)
                [ -n "$maj" ] && [ -n "$min" ] || continue
                # Our own node: /dev is about to be replaced by devtmpfs, and a
                # node made there would vanish with it.
                mkdir -p /tracedev 2>/dev/null
                mknod /tracedev/scratch b "$maj" "$min" 2>/dev/null && \
                    part=/tracedev/scratch
                break
            done
        fi
        if [ -n "$part" ] && [ -s "$TRACE" ]; then
            dd if="$TRACE" of="$part" bs=4096 conv=notrunc 2>/dev/null
            sync 2>/dev/null
        fi
        sleep 1
    done
) &

# Cap the trace so a loop cannot exhaust RAM. Truncating to the TAIL keeps the
# interesting part - where it stopped - rather than the boilerplate at the top.
(
    while : ; do
        sleep 5
        if [ -f "$TRACE" ]; then
            sz=$(wc -c < "$TRACE" 2>/dev/null || echo 0)
            if [ "${sz:-0}" -gt "$MAXBYTES" ] 2>/dev/null; then
                tail -c 2097152 "$TRACE" > "$TRACE.tmp" 2>/dev/null && \
                    mv "$TRACE.tmp" "$TRACE" 2>/dev/null
            fi
        fi
    done
) &

exec 9>>"$TRACE"
export BASH_XTRACEFD=9
export PS4='+ [${LINENO}] '
set -x

# Sourced, not exec'd: keeps PID 1, xtrace and fd 9 for the whole boot.
. /init.real
