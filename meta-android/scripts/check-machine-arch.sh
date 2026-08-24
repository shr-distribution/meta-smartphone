#!/bin/sh
# Report anything MACHINE_ARCH in a built feed.
#
# A universal rootfs is only universal if nothing device-specific is compiled
# into it. Yocto already tells us which packages those are: they are the ones
# that land in a MACHINE_ARCH feed directory rather than in an arch-wide one.
#
# Some of them are supposed to be there - OpenEmbedded emits a handful of
# per-machine config packages by design, and a device machine legitimately
# carries a kernel. Everything else is a porting bug: the file belongs in the
# device adaptation data, not in a package.
#
# Usage: check-machine-arch.sh [MACHINE] [DEPLOY_IPK_DIR]

set -eu

MACHINE=${1:-halium-arm64}
IPK=${2:-tmp/deploy/ipk}
FEED=$IPK/$(echo "$MACHINE" | tr - _)
[ -d "$FEED" ] || FEED=$IPK/$MACHINE

if [ ! -d "$FEED" ]; then
    echo "no feed at $FEED - nothing built for $MACHINE yet?"
    exit 0
fi

# Emitted per machine by OpenEmbedded itself; not our problem.
is_expected() {
    case $1 in
    base-files|opkg-arch-config|packagegroup-core-boot|systemd-conf| \
    systemd-machine-units|systemd-serialgetty|shadow-securetty| \
    udev-extraconf|udev-extraconf-automount|udev-extraconf-autonet| \
    keymaps|packagegroup-luneos-*|packagegroup-webos-*)
        return 0 ;;
    esac
    return 1
}

unexpected=""
for f in "$FEED"/*.ipk; do
    [ -e "$f" ] || continue
    p=$(basename "$f"); p=${p%%_*}
    # -dbg/-dev/-lic/-src/-doc are splits of the same recipe; judge the parent.
    base=$(echo "$p" | sed -E 's/-(dbg|dev|lic|src|doc|staticdev|locale-.*)$//')
    is_expected "$base" && continue
    unexpected="$unexpected$base\n"
done

n=$(printf "%b" "$unexpected" | sed '/^$/d' | sort -u | wc -l)
if [ "$n" -eq 0 ]; then
    echo "$MACHINE: feed is clean - nothing device-specific in the rootfs."
    exit 0
fi

echo "$MACHINE: $n recipes are still MACHINE_ARCH:"
printf "%b" "$unexpected" | sed '/^$/d' | sort -u | sed 's/^/    /'
echo
echo "Each is either device data that belongs in the adaptation repo, or a"
echo "recipe pinning PACKAGE_ARCH = MACHINE_ARCH that no longer needs to."
exit 1
