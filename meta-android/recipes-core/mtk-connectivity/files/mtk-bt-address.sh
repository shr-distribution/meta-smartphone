#!/bin/sh
# Seed BlueZ's board address from the MediaTek nvram/nvdata blob.
#
# bluebinder_post.sh knows how to read the BT MAC from Android/Qualcomm
# property paths, but MediaTek keeps it in a binary nvdata file with no
# corresponding property, so on MTK bluebinder fails with "Failed to get
# bluetooth address" and the adapter never comes up. The MAC is the first six
# bytes of BT_Addr. Write it once to the file bluebinder_post.sh checks first.
BTF=/mnt/vendor/nvdata/APCFG/APRDEB/BT_Addr
OUT=/var/lib/bluetooth/board-address

[ -f "$OUT" ] && exit 0
[ -f "$BTF" ] || exit 0

mac=$(hexdump -e '6/1 "%02X:"' -n 6 "$BTF" 2>/dev/null | sed 's/:$//')
case "$mac" in
    ??:??:??:??:??:??)
        # Reject the all-zero address; let bluebinder fall back instead.
        [ "$mac" = "00:00:00:00:00:00" ] && exit 0
        mkdir -p "$(dirname "$OUT")"
        echo "$mac" > "$OUT"
        ;;
esac
exit 0
