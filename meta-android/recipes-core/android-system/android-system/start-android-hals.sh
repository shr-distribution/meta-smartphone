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
# late_start is in here because init only reaches it on "on
# property:sys.boot_completed=1", which nothing sets on Halium - the framework
# that would set it never runs. On sargo that class holds ten vendor daemons,
# and one of them is the reason wifi did not work at all:
#
#   cnss-daemon    connects to the WLFW QMI service and votes for the modem
#       through per_mgr. Until it does, the WLAN firmware never finishes
#       booting: icnss completes ind_register, msa_info, msa_ready and cap and
#       then sits at FW_READY 0 forever, so the qcacld driver is never probed
#       and there is no wlan0. Starting it takes icnss straight to
#       0xd8f (... | FW READY | DRIVER PROBED) and wlan0/wlan1/p2p0 appear.
#   loc_launcher   GPS
#   vendor.fps_hal fingerprint
#
# The rest are diag and data-migration helpers that do nothing here.
CLASSES="hal core main late_start"

# Belt and braces for the above, in case a vendor rc declares one of these.
#
# Also skipped: RIL instances the board never uses. A Qualcomm vendor image
# declares qcrild, qcrild2 and qcrild3 whatever the board actually has, and
# marks the extra ones "disabled" so init leaves them alone unless a multisim
# property says otherwise - qcrild2 has such a trigger, qcrild3 has none in any
# rc file, so init would never start it. We start disabled services on purpose,
# because init halts before the triggers that would normally start them fire,
# but for a RIL instance with no slot behind it that means it exits at once and
# is respawned every 5s forever. Seen on sargo, where /vendor/etc/init/qcrild.rc
# declares all three and only two slots exist.
#
# Also skipped: ss_ramdump (/vendor/bin/subsystem_ramdump), which collects
# Qualcomm subsystem ramdumps for post-mortem debugging. It is "disabled" in
# init.<board>.rc and gated behind persist.vendor.sys.ssr.enable_ramdumps=1,
# which also creates the /data/vendor/ramdump dirs it writes to. With ramdumps
# off it finds no dirs and exits at once - same 5s respawn loop as above.
# Android's own vendor rc does "stop ss_ramdump" when ramdumps are disabled.
# Other hybris distros never hit this: SailfishOS, Droidian and UBports run
# droid-hal-init, which honours "disabled", and SailfishOS additionally ships a
# denylist (disabled_services.rc, _HYBRIS_DISABLED) covering the same class of
# debug/logging daemons - log_service, ylog, fwklog, poweronlog, slogmodem.
# We force-start disabled services, so we need the equivalent denylist here.
# To collect ramdumps: set the property, then "setprop ctl.start ss_ramdump".
#
# deviceinfo_android_skip_services extends this per device.
SKIP_SERVICES="bootanim bootanimation vendor.qcrild3 ss_ramdump"

# Services with a prerequisite the start loop below cannot see. qcrild reaches
# the modem over QMI, which needs rmt_storage (the modem's EFS backing store),
# per_mgr (subsystem bring-up) and qseecomd. The loop walks the rc files in
# readdir order, and on tissot qcrild.rc sorts ahead of the rc files declaring
# those - so qcrild went first, found no modem, called exit(), and segfaulted
# in its own static teardown on the way out:
#
#     __cxa_finalize -> ~SapModule -> android::sp<RadioConfigImpl>::operator=
#
# init restarts it and it loops - 13 to 16 core dumps in a single boot, with
# ofono still reporting "active" so nothing above notices.
#
# It only worked by accident: the apexd gate stalled init long enough that init
# had already started the prerequisites itself, so the loop skipped them as
# running. That is timing, not ordering, and it is why shortening the gate
# grace broke telephony. Starting these last makes the dependency explicit.
LATE_SERVICES="vendor.qcrild vendor.qcrild2 vendor.qcrild3 vendor.ril-daemon vendor.ril-daemon2"

# What the late services wait for, as init.svc.<name> entries that must read
# "running" before they are started.
LATE_PREREQS="vendor.rmt_storage vendor.per_mgr vendor.qseecomd"

for _di in /usr/share/luneos/adaptations/*/deviceinfo; do
    [ -f "$_di" ] && . "$_di" 2>/dev/null
done
[ -n "$deviceinfo_android_skip_services" ] &&
    SKIP_SERVICES="$SKIP_SERVICES $deviceinfo_android_skip_services"

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
#   - /system/bin/charger is Halium's own charging UI, from init.halium.rc:
#
#         on charger
#             start charger
#         service charger /system/bin/charger
#             class charger
#
#     It opens /dev/dri/card0 and takes DRM master to draw the battery
#     animation, and never gives it back - so the compositor can never become
#     master and sits in a retry loop forever: one thread in nanosleep, with
#     "Failed to become drm master" flooding logcat. It also respawns, so
#     killing it by hand is useless.
#
#     Most devices never hit this because they only enter charger mode when
#     genuinely powered off, and then the distro is not running anyway
#     (Droidian boots Android outright for bootreason charger|usb). The MP01
#     never truly powers off - holding power reboots it - so *every* boot with
#     a cable attached is a charger-mode boot and this fires every time.
DISPLAY_CONFLICT_MATCH="bootanimation surfaceflinger /system/bin/charger"

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
# init's own parser strips double quotes around rc arguments; awk does not.
# mt6739 writes wait_for_prop hwservicemanager.ready "true", and keeping the
# quotes both made the gate look eternally unsatisfied and, worse, "forced"
# the literal value \"true\" over the correct one hwservicemanager had set,
# wedging every libhidl WaitForProperty on the device.
# Is a gate satisfied? Not always a string compare: some of these properties are
# state machines, not flags, and a later state still means the gate was passed.
#
# apexd.status is the case that matters. apexd walks '' -> starting ->
# activated -> ready in about 2.5s, and "activated" is live for roughly 120ms
# before apexd-snapshotde moves it on - measured on tissot at 17.417s and
# 17.540s. The loop below polls every ~0.55s, so it hit that window about one
# boot in five; on the other four it concluded nothing would ever set the
# property and forced it, writing "activated" back over the "ready" that init
# actions key on (the GSI init.rc has on property:apexd.status=ready blocks).
#
# init is not blocked by any of this: its own wait_for_prop apexd.status
# activated completed in 408-489ms on every boot measured. Accepting the later
# state removes both the pointless wait and the incorrect write.
gate_satisfied() {
    _name=$1; _want=$2
    _have=$(getprop "$_name")
    [ "$_have" = "$_want" ] && return 0
    case "$_name" in
        apexd.status)
            # ready is past activated; starting is not.
            [ "$_want" = activated ] && [ "$_have" = ready ] && return 0
            ;;
    esac
    return 1
}

# A wait_for_prop only ever blocks init if the "on" block containing it runs.
# Emit each gate with its block header so the conditional ones can be filtered
# out below, rather than treating every wait_for_prop in the image as live.
gates_raw=$(all_rc_files | xargs -r awk '
    /^on /      { blk = $0; next }
    /^service / { blk = ""; next }
    $1 == "wait_for_prop" { gsub(/^"|"$/, "", $3); print blk "\t" $2 "=" $3 }
' 2>/dev/null | sort -u)

# Drop gates whose block is guarded by properties that do not hold. sargo has
# two wait_for_prop sys.trace.traced_started lines, and both sit under
# persist.debug.perfetto.* blocks that are unset on a normal boot - so the gate
# never blocks anything, yet the scan waited out the full grace period for it
# and then forced it. perfetto.rc says the property is "set by traced after
# listen()ing on the consumer socket", so setting it ourselves tells perfetto
# traced is ready when it may not be.
gates=
while IFS="	" read -r _blk _g; do
    [ -n "$_g" ] || continue
    _live=1
    for _c in $_blk; do
        case "$_c" in
            property:*)
                _cp=${_c#property:}
                [ "$(getprop "${_cp%%=*}")" = "${_cp#*=}" ] || _live=0
                ;;
        esac
    done
    if [ "$_live" = 1 ]; then
        gates="$gates $_g"
    else
        echo "ignoring inactive gate ${_g} (${_blk})"
    fi
done <<GATES_EOF
$gates_raw
GATES_EOF
gates=$(printf '%s\n' $gates | sort -u)
pending=
if [ -n "$gates" ]; then
    i=0
    # 10 half-seconds. Was 20, which was not a considered value: with the start
    # loop racing qcrild ahead of its prerequisites, the extra 5s was what let
    # init start them first, and shortening it broke telephony. With the
    # ordering explicit that slack is no longer load-bearing. Overridable so a
    # port that needs longer can have it without a patch.
    gate_grace=${ANDROID_GATE_GRACE:-10}
    while [ $i -lt "$gate_grace" ]; do
        pending=
        for g in $gates; do
            gate_satisfied "${g%%=*}" "${g#*=}" || pending="$pending $g"
        done
        [ -n "$pending" ] || break
        i=$((i + 1))
        sleep 0.5
    done
    for g in $pending; do
        # Re-check: the grace may have gone to fork overhead rather than to the
        # gate genuinely never being set.
        gate_satisfied "${g%%=*}" "${g#*=}" && continue
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
    case " $LATE_SERVICES " in *" $s "*) continue ;; esac
    [ "$(getprop init.svc.$s)" = "running" ] && continue
    setprop ctl.start "$s"
    started=$((started + 1))
done

echo "requested start of $started Android service(s) init skipped"

# Now the late set, once what it depends on is up. Bounded, and it starts them
# anyway on timeout: a device without these prerequisites should not lose its
# RIL over one that was never going to appear.
late_started=0
for s in $LATE_SERVICES; do
    case " $svcs " in *" $s "*) ;; *) continue ;; esac
    case " $SKIP_SERVICES " in *" $s "*) continue ;; esac
    [ "$(getprop init.svc.$s)" = "running" ] && continue
    i=0
    while [ $i -lt 40 ]; do
        waiting=
        for p in $LATE_PREREQS; do
            case " $svcs " in *" $p "*) ;; *) continue ;; esac
            [ "$(getprop init.svc.$p)" = "running" ] || waiting="$waiting $p"
        done
        [ -n "$waiting" ] || break
        i=$((i + 1))
        sleep 0.25
    done
    [ -n "$waiting" ] && echo "starting $s with$waiting not up after $((i / 4))s"
    setprop ctl.start "$s"
    late_started=$((late_started + 1))
done
[ "$late_started" -gt 0 ] && echo "requested start of $late_started modem service(s) after their prerequisites"

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
#
# The node is not always called "boot": qcacld built into the kernel rather
# than as a module registers /sys/kernel/boot_wlan/boot_wlan and loads the
# driver only when that is written, and UBports' sargo board rc drives it with
#
#     write /sys/kernel/boot_wlan/boot_wlan 1
#
# so match any file under a /sys/kernel/boot_* directory. Subsystems without an
# msm_subsys entry, which boot_wlan is one of, simply never look ONLINE and are
# written every time; the writes are one-shot loaders and idempotent.
dsp=0
for n in $(all_rc_files | xargs -r awk '$1 == "write" && $2 ~ /^\/sys\/kernel\/boot_[a-z0-9_]+\/[a-z0-9_]+$/ { print $2 }' 2>/dev/null | sort -u); do
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

# Re-enable the SoC low-power modes that the vendor init switched off.
#
# Qualcomm board rc files gate cpuidle and platform suspend on Android's boot
# finishing. sargo's init.sdm670.rc, for instance:
#
#     on init && property:vendor.skip.init=0
#         write /sys/module/lpm_levels/parameters/sleep_disabled 1
#     on property:sys.boot_completed=1
#         write /sys/module/lpm_levels/parameters/sleep_disabled 0
#
# The container runs "on init", so every boot ends with sleep_disabled=1 - and
# nothing here ever sets sys.boot_completed, so the write that undoes it never
# fires. Measured on sargo: with the flag set the cpuidle C1-C3 counters stay
# at exactly 0 on all eight cores and the SoC can never enter platform suspend;
# clearing it engaged the deep C-states within seconds and the device stayed
# stable. Dropping lpm_levels.sleep_disabled=1 from the kernel command line was
# necessary but not sufficient, because of this write.
#
# Write 0 ourselves once the HALs are up. The blanket "write" exclusion above
# still stands; this is the one boot_completed write whose absence costs more
# than an idle device's whole battery, and it is a plain module parameter,
# not a hardware poke. Boards whose rc files never set it (tissot's
# init.target.rc only writes 0) and kernels without lpm_levels are untouched.
LPM_SLEEP_DISABLED=/sys/module/lpm_levels/parameters/sleep_disabled
if [ -w "$LPM_SLEEP_DISABLED" ]; then
    if echo 0 > "$LPM_SLEEP_DISABLED" 2>/dev/null; then
        echo "lpm_levels: sleep_disabled cleared (now $(cat "$LPM_SLEEP_DISABLED"))"
    else
        echo "WARNING: could not clear $LPM_SLEEP_DISABLED"
    fi
fi

# Do not return until the composer HAL is actually registered on hwbinder.
#
# Ordering surface-manager after this unit is necessary but not sufficient:
# ctl.start above only asks init to fork the service, it does not wait for it to
# publish its HIDL interface. If we return too early the compositor still races
# Probe with binder-ping (libgbinder-tools), NOT lshal. lshal is a heavy
# Android/bionic binary run through the container's linker namespace; run this
# early - before /linkerconfig/ld.config.txt and hwservicemanager are ready -
# it SIGSEGVs instead of failing cleanly, and retrying it dumped core dozens of
# times and dragged start-post past its timeout. binder-ping talks straight to
# /dev/hwbinder via libgbinder, returns a clean exit code (0 = registered and
# answered), and never crashes in the not-ready state.
#
# The composer registers under its highest implemented version and hwbinder
# lookups are exact, so try each known version rather than a wildcard.
COMPOSER_VERSIONS="2.4 2.3 2.2 2.1"

if command -v binder-ping >/dev/null 2>&1; then
    i=0
    registered=
    while [ $i -lt 100 ]; do
        for v in $COMPOSER_VERSIONS; do
            if binder-ping -q "android.hardware.graphics.composer@${v}::IComposer/default" 2>/dev/null; then
                echo "composer HAL @${v} registered on hwbinder after ${i}00ms"
                registered=1
                break
            fi
        done
        [ -n "$registered" ] && break
        i=$((i + 1))
        sleep 0.3
    done
    [ -n "$registered" ] || echo "WARNING: composer HAL never registered; compositor may take the in-process fallback"
else
    # Fallback if libgbinder-tools is missing: a bounded getprop poll. Weaker
    # (a running service has not necessarily finished registerAsService) but it
    # never crashes or hangs.
    echo "WARNING: binder-ping not found; falling back to getprop composer check"
    i=0
    while [ $i -lt 60 ]; do
        if getprop | grep -E '^\[init\.svc\.(vendor\.)?hwcomposer' | grep -q '\[running\]'; then
            echo "composer HAL service running (getprop)"
            break
        fi
        i=$((i + 1))
        sleep 0.5
    done
fi

exit 0
