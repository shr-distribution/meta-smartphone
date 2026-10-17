#!/bin/sh
# Hand mako's Bluetooth address to the Android Bluetooth HAL and to bluebinder.
#
# The factory address is in /persist/bluetooth/.bdaddr: six bytes, most significant first (10:68:3F:...
# is LG's OUI). The Android 9 HAL (android.hardware.bluetooth@1.0-impl) aborts on open with "No Bluetooth
# Address!" unless ro.bt.bdaddr_path, ro.boot.btmacaddr or persist.service.bdroid.bdaddr gives it one, and
# none is set in the container; bluebinder then only logs "Remote has died". bluebinder_post.sh in turn
# fails the unit unless it finds an address, and it looks in /var/lib/bluetooth/board-address first.
# The controller itself is given the address in the container, by the vendor image's mako-bt-init.sh.
SRC=/persist/bluetooth/.bdaddr
OUT=/var/lib/bluetooth/board-address

if [ ! -s "$SRC" ]; then
    echo "mako-bt-address: no $SRC"
    exit 0
fi

mac=$(hexdump -v -n 6 -e '6/1 "%02X:"' "$SRC" | sed 's/:$//')
case "$mac" in
    00:00:00:00:00:00|"")
        echo "mako-bt-address: $SRC holds no address"
        exit 0
        ;;
    ??:??:??:??:??:??) ;;
    *)
        echo "mako-bt-address: cannot read $SRC ($mac)"
        exit 0
        ;;
esac

mkdir -p "$(dirname "$OUT")"
[ -f "$OUT" ] || echo "$mac" > "$OUT"

# The container's property service answers only once Android's init is up.
i=0
while [ -z "$(/usr/bin/getprop ro.build.version.sdk 2>/dev/null)" ] && [ "$i" -lt 60 ]; do
    sleep 1
    i=$((i + 1))
done
if /usr/bin/setprop persist.service.bdroid.bdaddr "$mac"; then
    echo "mako-bt-address: $mac"
else
    echo "mako-bt-address: setprop failed"
fi
exit 0
