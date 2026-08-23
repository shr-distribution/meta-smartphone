#!/bin/sh
# Start the vendor HAL services the container's init never got to.
#
# Android's init never reaches "on boot" - which is where AOSP puts
# "class_start hal" and "class_start core" - so every service in those classes
# stays dead. Note that init is not curtailed by design here, as it first
# appears: it is *stuck* on a wait_for_prop gate (see the gate-unblocking pass
# below). Opening that gate is the real fix and makes most of this script a
# no-op; what remains is a safety net for images where a gate cannot be opened. On a Pixel 3a that is
# 33 of 53 vendor services, including sensors, audio, camera, bluetooth, thermal,
# GPS, health, light and the modem. The only display HAL that survives does so by
# accident: hwcomposer is declared "class hal animation", and animation starts
# early.
#
# Nothing in the container fixes this for us. "on property:sys.boot_from_charger_mode=1"
# looks like the intended lever but merely does "trigger late-init", which re-runs
# the same chain and halts in the same place.
#
# So do what class_start would have done, driven by the container's own rc files
# rather than a per-device list - a device-agnostic GSI plus the device's own
# vendor is exactly the case this has to work for.
#
# This runs as ExecStartPost of android-system.service, before the compositor and
# the rest of the stack start. Ordering matters: starting these against an already
# running compositor wedged it, presumably contending for gralloc/allocator.

# Vendor and odm only. The GSI's own /system services are deliberately excluded:
# Halium curtails the boot on purpose, and pulling in the framework side brings
# things like bootanimation, which never exits without SurfaceFlinger to tell it
# to and then contends with the real compositor for the display.
ANDROID_INIT_DIRS="/android/vendor/etc/init /android/odm/etc/init"
CLASSES="hal core main"

# Belt and braces for the above, in case a vendor rc declares one of these.
SKIP_SERVICES="bootanim bootanimation"

# Services that must not run at all, because they fight the host compositor for
# the display. These are NOT started by us - init reaches "on late-fs" long
# before it halts, and QTI's init.<board>.rc starts surfaceflinger, bootanim and
# the composer HAL from there - so skipping them above is not enough and they
# have to be stopped explicitly.
#
# Why they are harmful rather than merely idle:
#   - bootanimation waits for a SurfaceFlinger that Halium never starts, so it
#     polls once a second forever and never exits.
#
# NOTE: android.hardware.graphics.composer@X.Y-service is deliberately NOT in
# this list, even though it is what wins DRM master on a cold boot and thereby
# causes the stalled-compositor symptom. Stopping it was measured on sargo and
# makes things strictly worse: with the composer service stopped, surface-manager
# hangs during startup at five threads and never creates its QML, render or
# input threads, so the UI never comes up at all. The service is required; the
# problem is purely that it opens /dev/dri/card0 first. See the ordering note in
# android-system.service for how that race is actually addressed.
DISPLAY_CONFLICT_MATCH="bootanimation surfaceflinger"

command -v setprop >/dev/null 2>&1 || { echo "setprop not available, skipping"; exit 0; }

# Includes the hw/ subdirectory. init.<board>.rc lives there and declares the
# services the SoC's own subsystems depend on - on a Pixel 3a that is 28 of the
# 29 services in these classes, and the ones that matter most:
#
#   pd_mapper, vendor.per_mgr, vendor.per_proxy   protection-domain and
#       peripheral managers. The SSC sensors run in a protection domain on the
#       ADSP, and without these the sensor daemon discovers nothing at all.
#   vendor.rmt_storage                            serves the modem its EFS out
#       of /mnt/vendor/persist. Boot the modem without it and it crashes, and
#       since its restart_level is SYSTEM that reboots the phone.
#   adsprpcd, adsprpcd_sensorspd, cdsprpcd        DSP RPC daemons.
#
# The scan used to stop at the top level to avoid pulling in framework
# services, but that concern is about /android/system/etc/init, which is still
# excluded here - vendor and odm rc files declare vendor services wherever they
# sit. The display-conflict scan below already reads hw/, so surfaceflinger and
# bootanim are still filtered out.
svcs=$(
    for d in $ANDROID_INIT_DIRS; do
        [ -d "$d" ] || continue
        for f in "$d"/*.rc "$d"/hw/*.rc; do
            [ -f "$f" ] || continue
            awk -v classes="$CLASSES" '
                /^service /            { svc = $2; next }
                /^[[:space:]]*class /  {
                    if (svc == "") next
                    for (i = 2; i <= NF; i++)
                        if (index(" " classes " ", " " $i " ")) { print svc; break }
                    svc = ""
                }
            ' "$f"
        done
    done | sort -u
)

[ -n "$svcs" ] || { echo "no vendor services found to start"; exit 0; }

# All rc files, including the hw/ subdirectory, same as the start scan above.
# The board rc (init.<board>.rc) lives there and is where both the
# display-conflict services and the bulk of the permission setup are declared.
# The conflict/permission scan looks wider than the start scan: it also takes in
# the GSI's own /system/etc/init. bootanimation is declared only there, while
# being *started* from the board rc under vendor - so a vendor-only scan sees the
# "start bootanim" but never the "service bootanim" it needs to stop it. Widening
# the start scan instead is not an option: that is exactly how the framework
# services would get pulled in.
ALL_INIT_DIRS="$ANDROID_INIT_DIRS /android/system/etc/init"

all_rc_files() {
    for d in $ALL_INIT_DIRS; do
        [ -d "$d" ] || continue
        for f in "$d"/*.rc "$d"/hw/*.rc; do
            [ -f "$f" ] && echo "$f"
        done
    done
}

# Unblock init's wait_for_prop gates.
#
# This is the actual reason init never reaches "on boot": it is not curtailed by
# design, it is *stuck*. init.<board>.rc gates post-fs-data on
#
#     wait_for_prop vendor.qcom.time.set true
#
# and that property is set by Android's time_daemon, which Halium does not run
# because it fights the host's timekeeping. wait_for_prop blocks init's state
# machine outright, so early-boot and boot never run: no class_start, and none
# of the chown/chmod that the vendor HALs depend on. UBports hit the same wall
# on this device and set the property from their lxc-android-config device-hacks.
#
# Rather than hardcode one vendor's property name, take every gate the rc files
# declare, give whatever legitimately sets them a grace period, and only then
# force the stragglers. On sargo four of the five gates satisfy themselves.
gates=$(all_rc_files | xargs -r awk '$1 == "wait_for_prop" { print $2 "=" $3 }' 2>/dev/null | sort -u)
pending=
if [ -n "$gates" ]; then
    i=0
    while [ $i -lt 20 ]; do
        pending=
        for g in $gates; do
            [ "$(getprop "${g%%=*}")" = "${g#*=}" ] || pending="$pending $g"
        done
        [ -n "$pending" ] || break
        i=$((i + 1))
        sleep 0.5
    done
    for g in $pending; do
        echo "forcing init gate ${g%%=*}=${g#*=} (nothing in this image sets it)"
        setprop "${g%%=*}" "${g#*=}"
    done
    [ -n "$pending" ] && sleep 3
fi

# Resolve the display-conflict matches to service names up front, so the start
# loop below can skip them instead of starting them only for us to stop them
# again a moment later.
conflict_svcs=""
for e in $(all_rc_files | xargs -r awk '/^service /{print $2 "=" $3}' 2>/dev/null | sort -u); do
    for m in $DISPLAY_CONFLICT_MATCH; do
        case "${e#*=}" in *"$m"*) conflict_svcs="$conflict_svcs ${e%%=*}"; break ;; esac
    done
done

started=0
for s in $svcs; do
    case " $SKIP_SERVICES " in *" $s "*) continue ;; esac
    case " $conflict_svcs " in *" $s "*) continue ;; esac
    [ "$(getprop init.svc.$s)" = "running" ] && continue
    setprop ctl.start "$s"
    started=$((started + 1))
done

echo "requested start of $started Android service(s) init skipped"

# Stop the ones init already started from "on late-fs" before we got here.
stopped=0
for s in $conflict_svcs; do
    [ "$(getprop init.svc.$s)" = "stopped" ] && continue
    setprop ctl.stop "$s"
    stopped=$((stopped + 1))
done
echo "requested stop of $stopped display-conflicting Android service(s)"

# Replay the permission setup from the triggers init never reaches.
#
# "on early-boot" and "on boot" are where the board rc chowns and chmods the
# vendor sysfs nodes its HALs need. Because init halts after post-fs-data those
# actions never run, so the nodes keep their kernel defaults (root:root 0644)
# while the HALs run as system/audio/graphics. The result is a boot full of
# EACCES: the vibrator and qcrild wedge in permanent restart loops, the sensors
# HAL cannot take a wakelock, and Diag_LSM_Init fails with error 13.
#
# Only chown/chmod/mkdir are replayed. "write" is deliberately left alone: those
# poke hardware state rather than permissions, and firing them out of order,
# after the rest of the stack has already come up, is not obviously safe.
fixed=0
for cmd in chown chmod mkdir; do
    all_rc_files | xargs -r awk -v want="$cmd" '
        /^on /      { inblk = ($0 ~ /^on (early-boot|boot)[[:space:]]*$/); next }
        /^service / { inblk = 0; next }
        inblk && $1 == want { print }
    ' 2>/dev/null
done | sort -u | while read -r verb a b c; do
    case "$verb" in
        chown) [ -e "$c" ] && chown "$a:$b" "$c" 2>/dev/null && echo "$c" ;;
        chmod) [ -e "$b" ] && chmod "$a"     "$b" 2>/dev/null && echo "$b" ;;
        mkdir) [ -e "$a" ] || { mkdir -p "$a" 2>/dev/null && echo "$a" ; } ;;
    esac
done | wc -l | while read -r fixed; do
    echo "applied init permissions to $fixed node(s) init skipped"
done

# Boot the DSP subsystems "on early-boot" would have.
#
# The blanket "write" exclusion above still stands, but the QDSP6 boot nodes are
# the one case that cannot be skipped. /sys/kernel/boot_adsp/boot is what loads
# the audio DSP, and until something writes it the ASoC machine driver never
# registers a card at all: /proc/asound/cards stays empty, the vendor audio HAL
# fails every mixer open --
#
#     E audio_hw_utils: audio_extn_utils_get_snd_card_num:
#         Unable to open the mixer card: 0 ... 7
#
# -- module-droid-card fails to load, and pulseaudio aborts on startup. The
# sensors on this SoC also live on the ADSP, so nothing in the SSC stack
# enumerates either.
#
# Taken from the rc files rather than hardcoded, so a device with a different
# set of DSPs gets its own. Guarded on the subsystem not already being ONLINE,
# because writing the node asks the subsystem framework for another load.
dsp=0
for n in $(all_rc_files | xargs -r awk '$1 == "write" && $2 ~ /^\/sys\/kernel\/boot_[a-z0-9]+\/boot$/ { print $2 }' 2>/dev/null | sort -u); do
    [ -e "$n" ] || continue
    name=${n#/sys/kernel/boot_}
    name=${name%/boot}
    online=0
    for d in /sys/bus/msm_subsys/devices/*; do
        [ "$(cat "$d/name" 2>/dev/null)" = "$name" ] || continue
        [ "$(cat "$d/state" 2>/dev/null)" = "ONLINE" ] && online=1
    done
    [ "$online" = 1 ] && continue
    if echo 1 > "$n" 2>/dev/null; then
        dsp=$((dsp + 1))
    else
        echo "WARNING: could not boot the $name DSP via $n"
    fi
done
echo "booted $dsp DSP subsystem(s) init skipped"

# Do not return until the composer HAL is actually registered on hwbinder.
#
# Ordering surface-manager after this unit is necessary but not sufficient:
# ctl.start above only asks init to fork the service, it does not wait for it to
# publish its HIDL interface. If we return too early the compositor still races
# it and still takes the in-process fallback described in android-system.service.
# lshal is not on the default PATH the way getprop is, so resolve it explicitly
# rather than letting "command -v lshal" fail and silently skip the wait.
LSHAL=
for c in lshal /android/system/bin/lshal /system/bin/lshal; do
    command -v "$c" >/dev/null 2>&1 && { LSHAL=$c; break; }
done

if [ -n "$LSHAL" ]; then
    i=0
    while [ $i -lt 60 ]; do
        if $LSHAL 2>/dev/null | grep -q "android.hardware.graphics.composer@.*::IComposer/default"; then
            echo "composer HAL registered on hwbinder after ${i}00ms"
            break
        fi
        i=$((i + 1))
        sleep 0.5
    done
    [ $i -lt 60 ] || echo "WARNING: composer HAL never registered; compositor may take the in-process fallback"
else
    echo "WARNING: lshal not found; cannot confirm composer HAL registration"
fi

exit 0
