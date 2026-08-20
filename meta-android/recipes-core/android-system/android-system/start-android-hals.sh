#!/bin/sh
# Start the vendor HAL services the container's init never got to.
#
# Halium curtails Android's boot sequence so the framework never comes up: init
# runs early-init through post-fs-data and then stops. "on boot" - which is where
# AOSP puts "class_start hal" and "class_start core" - is therefore never
# processed, so every service in those classes stays dead. On a Pixel 3a that is
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

command -v setprop >/dev/null 2>&1 || { echo "setprop not available, skipping"; exit 0; }

svcs=$(
    for d in $ANDROID_INIT_DIRS; do
        [ -d "$d" ] || continue
        for f in "$d"/*.rc; do
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

started=0
for s in $svcs; do
    case " $SKIP_SERVICES " in *" $s "*) continue ;; esac
    [ "$(getprop init.svc.$s)" = "running" ] && continue
    setprop ctl.start "$s"
    started=$((started + 1))
done

echo "requested start of $started Android service(s) init skipped"
exit 0
