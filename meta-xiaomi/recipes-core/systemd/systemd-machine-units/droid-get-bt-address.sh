#!/bin/sh
# Seed BlueZ's board address from the Qualcomm Bluetooth NV item.
#
# bluebinder_post.sh only knows the address from ro.bt.bdaddr_path,
# ro.vendor.bt.bdaddr_path or persist.vendor.service.bdroid.bdaddr, and the
# msm8953 Android 9 vendor sets none of them: the controller reads its address
# straight from /persist/bluetooth/.bt_nv.bin. With nothing to copy the script
# prints "Failed to get bluetooth address!", the failing ExecStartPost stops
# bluebinder right after it set up vhci, and there is no hci0.
#
# bluebinder_post.sh runs this hook first and is satisfied once
# /var/lib/bluetooth/board-address exists.
#
# .bt_nv.bin is one NV item: 0x01 (tag), 0x00, 0x06 (length), then the six
# address bytes least significant first.
NV=/mnt/vendor/persist/bluetooth/.bt_nv.bin
[ -f "$NV" ] || NV=/android/persist/bluetooth/.bt_nv.bin
OUT=/var/lib/bluetooth/board-address

[ -f "$OUT" ] && exit 0
[ -f "$NV" ] || exit 0

# shellcheck disable=SC2046 # split into one positional per byte
set -- $(hexdump -v -e '1/1 "%02X "' -n 9 "$NV" 2>/dev/null)
[ $# -eq 9 ] && [ "$1" = "01" ] && [ "$3" = "06" ] || exit 0

mac="$9:$8:$7:$6:$5:$4"
case "$mac" in
    00:00:00:00:00:00|FF:FF:FF:FF:FF:FF) exit 0 ;;
esac

mkdir -p "${OUT%/*}"
echo "$mac" > "$OUT"
exit 0
